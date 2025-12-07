package com.weather.forecast.service.impl;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
import com.weather.forecast.model.Forecast;
import com.weather.forecast.repository.CurrentWeatherRepository;
import com.weather.forecast.repository.ForecastRepository;
import com.weather.forecast.service.CityService;
import com.weather.forecast.service.OpenWeatherMapClient;
import com.weather.forecast.service.WeatherService;
import com.weather.forecast.util.WeatherMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);

    private final OpenWeatherMapClient weatherClient;
    private final CityService cityService;
    private final CurrentWeatherRepository currentWeatherRepository;
    private final ForecastRepository forecastRepository;
    private final WeatherMapper weatherMapper;

    public WeatherServiceImpl(OpenWeatherMapClient weatherClient, CityService cityService, CurrentWeatherRepository currentWeatherRepository, ForecastRepository forecastRepository, WeatherMapper weatherMapper) {
        this.weatherClient = weatherClient;
        this.cityService = cityService;
        this.currentWeatherRepository = currentWeatherRepository;
        this.forecastRepository = forecastRepository;
        this.weatherMapper = weatherMapper;
    }

    @Override
    @Cacheable(value = "currentWeather", key = "#cityName.toLowerCase()")
    public WeatherResponse getCurrentWeather(String cityName) {
        try {
            City city = cityService.findOrCreateCity(cityName);
            cityService.incrementSearchCount(city);

            Optional<CurrentWeather> existingWeather = currentWeatherRepository.findByCityId(city.getId());

            if (existingWeather.isPresent() && weatherMapper.isDataFresh(existingWeather.get().getLastUpdated())) {
                return weatherMapper.mapToWeatherResponse(existingWeather.get());
            }

            OpenWeatherMapResponse apiResponse = weatherClient.getCurrentWeather(cityName);
            weatherMapper.updateCityFromResponse(city, apiResponse);

            CurrentWeather currentWeather = weatherMapper.mapToCurrentWeather(city, apiResponse);
            currentWeather = currentWeatherRepository.save(currentWeather);

            return weatherMapper.mapToWeatherResponse(currentWeather);
        } catch (WeatherApiException e) {
            logger.error("Error fetching current weather for {}: {}", cityName, e.getMessage());

            return cityService.findByName(cityName)
                    .flatMap(city -> currentWeatherRepository.findByCityId(city.getId()))
                    .map(weatherMapper::mapToWeatherResponse)
                    .orElseThrow(() -> e); // Rethrow original exception if no stale data exists
        }
    }

    @Override
    @Cacheable(value = "forecast", key = "#cityName.toLowerCase()")
    public ForecastResponse getForecast(String cityName) {
        try {
            City city = cityService.findOrCreateCity(cityName);

            List<Forecast> existingForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
                    city.getId(), Instant.now()
            );

            if (!existingForecasts.isEmpty() && weatherMapper.isDataFresh(existingForecasts.get(0).getForecastDate().minus(1, ChronoUnit.DAYS))) {
                return weatherMapper.mapToForecastResponse(city, existingForecasts);
            }

            OpenWeatherMapForecastResponse apiResponse = weatherClient.getForecast(cityName);
            weatherMapper.updateCityFromResponse(city, apiResponse);

            List<Forecast> newForecasts = refreshForecastDataForCity(city, apiResponse);

            return weatherMapper.mapToForecastResponse(city, newForecasts);
        } catch (WeatherApiException e) {
            logger.error("Error fetching forecast for {}: {}", cityName, e.getMessage());

            City city = cityService.findByName(cityName).orElseThrow(() -> new CityNotFoundException("City not found: " + cityName));
            List<Forecast> existingForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(city.getId(), Instant.now().minus(1, ChronoUnit.DAYS));

            if (!existingForecasts.isEmpty()) {
                return weatherMapper.mapToForecastResponse(city, existingForecasts);
            }

            throw e;
        }
    }

    @Override
    @Transactional
    public void refreshWeatherData(City city) {
        try {
            logger.info("Refreshing weather data for {}", city.getName());

            // Fetch and update current weather
            OpenWeatherMapResponse currentResponse = weatherClient.getCurrentWeather(city.getName());
            CurrentWeather currentWeather = weatherMapper.mapToCurrentWeather(city, currentResponse);
            currentWeatherRepository.save(currentWeather);

            // Fetch and update forecast data
            OpenWeatherMapForecastResponse forecastResponse = weatherClient.getForecast(city.getName());

            // Call the reusable helper method
            refreshForecastDataForCity(city, forecastResponse);

            logger.info("Successfully refreshed weather data for {}", city.getName());
        } catch (WeatherApiException e) {
            logger.error("Error refreshing weather data for city {}: {}", city.getName(), e.getMessage());
            // No need to rethrow here unless a calling scheduler needs to know about the failure.
        }
    }

    private List<Forecast> refreshForecastDataForCity(City city, OpenWeatherMapForecastResponse forecastResponse) {
        forecastRepository.deleteByCityId(city.getId());
        List<Forecast> forecasts = weatherMapper.mapToForecasts(city, forecastResponse);
        return forecastRepository.saveAll(forecasts);
    }
}