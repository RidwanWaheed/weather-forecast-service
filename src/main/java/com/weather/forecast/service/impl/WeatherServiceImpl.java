package com.weather.forecast.service.impl;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
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
        try{
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
        } catch (WeatherApiException e){
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
    public ForecastResponse getForecast(String cityName) {
        return null;
    }

    @Override
    public void refreshWeatherData(City city) {

    }
}
