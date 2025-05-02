package com.weather.forecast.service;


import com.weather.forecast.model.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeatherDataScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WeatherDataScheduler.class);

    private final CityService cityService;
    private final WeatherService weatherService;

    public WeatherDataScheduler(CityService cityService, WeatherService weatherService) {
        this.cityService = cityService;
        this.weatherService = weatherService;
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void refreshWeatherData() {
        logger.info("Starting scheduled weather data refresh");

        List<City> frequentlySearchedCities = cityService.getFrequentlySearchedCities(10);

        logger.info("Refreshing weather data for {} cities", frequentlySearchedCities.size());

        for (City city : frequentlySearchedCities) {
            try {
                weatherService.refreshWeatherData(city);
            } catch (Exception e) {
                logger.error("Error refreshing weather data for {}: {}", city.getName(), e.getMessage());
            }
        }
        logger.info("Completed scheduled weather data refresh");
    }
}
