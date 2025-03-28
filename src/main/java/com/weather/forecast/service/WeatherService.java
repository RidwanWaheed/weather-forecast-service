package com.weather.forecast.service;

import com.weather.forecast.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    // Inject API Key and Base URL from application.properties
    @Value("${openweathermap.api.key}")
    private String apiKey; // API key for authentication

    @Value("${openweathermap.api.url}")
    private String apiUrl; // Base URL of OpenWeatherMap API

    private final RestTemplate restTemplate; // Used to send HTTP requests

    // Constructor injection for RestTemplate
    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Method to get weather data for a given city
    public WeatherResponse getWeather(String city) {
        // Construct the complete URL with city and API key
        String url = String.format("%s/weather?q=%s&appid=%s", apiUrl, city, apiKey);

        // Make the HTTP GET request and map the response to WeatherResponse object
        return restTemplate.getForObject(url, WeatherResponse.class);
    }
}