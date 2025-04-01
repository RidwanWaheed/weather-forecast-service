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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
    @Transactional()
    public WeatherResponse getCurrentWeather(String cityName) {
        try {
            // Find or create city and increment search count
            City city = cityService.findOrCreateCity(cityName);
            cityService.incrementSearchCount(city);

            // Try to get current weather from database first
            Optional<CurrentWeather> existingWeather = currentWeatherRepository.findById(city.getId());

            // If weather data exists and is recent (less than 30 minutes old), use it
            if (existingWeather.isPresent() && weatherMapper.isDataFresh(existingWeather.get().getLastUpdated())) {
                return weatherMapper.mapToWeatherResponse(existingWeather.get());
            }

            // Otherwise, fetch from API and save
            OpenWeatherMapResponse apiResponse = weatherClient.getCurrentWeather(cityName);

            // Update city data with coordinates from API
            weatherMapper.updateCityFromResponse(city, apiResponse);

            // Create weather entity from API response
            CurrentWeather currentWeather = weatherMapper.mapToCurrentWeather(city, apiResponse);
            currentWeather = currentWeatherRepository.save(currentWeather);

            return weatherMapper.mapToWeatherResponse(currentWeather);
        } catch (WeatherApiException e) {
            logger.error("Error fetching current weather for {}: {}", cityName, e.getMessage());

            // Try to return cached data even if it's stale
            City city = cityService.findByName(cityName)
                    .orElseThrow(() -> new CityNotFoundException("City not found: " + cityName));

            Optional<CurrentWeather> existingWeather = currentWeatherRepository.findById(city.getId());
            if (existingWeather.isPresent()) {
                return weatherMapper.mapToWeatherResponse(existingWeather.get());
            }

            // If no data available, rethrow
            throw e;
        }
    }

    @Override
    @Cacheable(value = "forecast", key = "#cityName.toLowerCase()")
    @Transactional
    public ForecastResponse getForecast(String cityName) {
        try {
            // Find or Create city
            City city = cityService.findOrCreateCity(cityName);

            //Check if we have recent forecast data
            List<Forecast> existingForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
                    city.getId(), LocalDateTime.now()
            );

            //if we have recent forecasts, use them
            if (!existingForecasts.isEmpty() && weatherMapper.isDataFresh(existingForecasts.get(0).getForecastDate().minusDays(1))) {
                return weatherMapper.mapToForecastResponse(city, existingForecasts);
            }

            // Otherwise, fetch from API
            OpenWeatherMapForecastResponse apiResponse = weatherClient.getForecast(cityName);

            //Update city data
            weatherMapper.updateCityFromResponse(city, apiResponse);

            // Clear existing forecasts for this city
            List<Forecast> currentForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
                    city.getId(), LocalDateTime.now().minusDays(1)
            );

            forecastRepository.deleteAll(currentForecasts);

            // Save new forecast data
            List<Forecast> forecasts = weatherMapper.mapToForecasts(city, apiResponse);
            forecastRepository.saveAll(forecasts);

            return weatherMapper.mapToForecastResponse(city, forecasts);
        } catch (WeatherApiException e) {
            logger.error("Error fetching forecast for {}: {}", cityName, e.getMessage());

            // Try to return cached data even if it's stale
            City city = cityService.findByName(cityName).orElseThrow(() -> new CityNotFoundException("City not found: " + cityName));

            List<Forecast> existingForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(city.getId(), LocalDateTime.now().minusDays(1));

            if (!existingForecasts.isEmpty()) {
                return weatherMapper.mapToForecastResponse(city, existingForecasts);
            }

            // If no data available, rethrow
            throw e;
        }
    }

    @Override
    public void refreshWeatherData(City city) {
        try {
            logger.info("Refreshing weather data for {}", city.getName());

            // Fetch and update current weather
            OpenWeatherMapResponse currentResponse = weatherClient.getCurrentWeather(city.getName());
            CurrentWeather currentWeather = weatherMapper.mapToCurrentWeather(city, currentResponse);
            currentWeatherRepository.save(currentWeather);

            // Fetch and update forecast data
            OpenWeatherMapForecastResponse forecastResponse = weatherClient.getForecast(city.getName());

            // Clear outdated forecasts
            List<Forecast> currentForecasts = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(city.getId(), LocalDateTime.now().minusDays(1));
            forecastRepository.deleteAll(currentForecasts);

            // Save new forecasts
            List<Forecast> forecasts = weatherMapper.mapToForecasts(city, forecastResponse);
            forecastRepository.saveAll(forecasts);

            logger.info("Successfully refreshed weather data for {}", city.getName());

        } catch (WeatherApiException e) {
            logger.error("Error refreshing weather data for city {}: {}", city.getName(), e.getMessage());
            throw e;
        }
    }

}
