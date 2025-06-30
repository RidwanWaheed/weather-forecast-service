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
import org.springframework.transaction.annotation.Transactional; // Use the Spring annotation

import java.time.LocalDateTime;
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
    @Transactional
    public WeatherResponse getCurrentWeather(String cityName) {
        try {
            City city = cityService.findOrCreateCity(cityName);
            cityService.incrementSearchCount(city);

            Optional<CurrentWeather> existingWeather = currentWeatherRepository.findById(city.getId());

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
                    // Use a lambda to extract the city's ID and pass it to findById
                    .flatMap(city -> currentWeatherRepository.findById(city.getId()))
                    .map(weatherMapper::mapToWeatherResponse)
                    .orElseThrow(() -> e); // Rethrow original exception if no stale data exists
        }
    }

    @Override
    @Cacheable(value = "forecast", key = "#cityName.toLowerCase()")
    @Transactional
    public ForecastResponse getForecast(String cityName) {
        try {
            City city = cityService.findOrCreateCity(cityName);

            List<Forecast> existingForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
                    city.getId(), LocalDateTime.now()
            );

            if (!existingForecasts.isEmpty() && weatherMapper.isDataFresh(existingForecasts.get(0).getForecastDate().minusDays(1))) {
                return weatherMapper.mapToForecastResponse(city, existingForecasts);
            }

            OpenWeatherMapForecastResponse apiResponse = weatherClient.getForecast(cityName);
            weatherMapper.updateCityFromResponse(city, apiResponse);

            // This now calls our new, efficient helper method
            List<Forecast> newForecasts = refreshForecastDataForCity(city, apiResponse);

            return weatherMapper.mapToForecastResponse(city, newForecasts);
        } catch (WeatherApiException e) {
            logger.error("Error fetching forecast for {}: {}", cityName, e.getMessage());

            // Try to return cached data even if it's stale
            City city = cityService.findByName(cityName).orElseThrow(() -> new CityNotFoundException("City not found: " + cityName));
            List<Forecast> existingForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(city.getId(), LocalDateTime.now().minusDays(1));

            if (!existingForecasts.isEmpty()) {
                return weatherMapper.mapToForecastResponse(city, existingForecasts);
            }

            throw e;
        }
    }

    @Override
    // IMPROVEMENT: Added @Transactional for data consistency.
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

    /**
     * IMPROVEMENT: A private helper method to encapsulate the logic for clearing and
     * saving new forecast data. This promotes code reuse and simplifies the public methods.
     */
    private List<Forecast> refreshForecastDataForCity(City city, OpenWeatherMapForecastResponse forecastResponse) {
        // IMPROVEMENT: Use the efficient bulk-delete method from the repository.
        forecastRepository.deleteByCityId(city.getId());

        // Save new forecasts
        List<Forecast> forecasts = weatherMapper.mapToForecasts(city, forecastResponse);
        return forecastRepository.saveAll(forecasts);
    }
}