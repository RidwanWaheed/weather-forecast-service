package com.weather.forecast.service;


import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.exception.WeatherApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class OpenWeatherMapClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapClient.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String units;

    public OpenWeatherMapClient(RestTemplate restTemplate, @Value("${openweathermap.api.url}") String apiUrl, @Value("${openweathermap.api.key}") String apiKey, @Value("${openweathermap.api.units}") String units) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.units = units;
    }

    public OpenWeatherMapResponse getCurrentWeather(String city) {
        String url = UriComponentsBuilder
                .fromUriString(apiUrl + "/weather")
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", units)
                .build()
                .toUriString();

        logger.debug("Fetching current weather for {} from: {}", city, url.replace(apiKey, "API_KEY"));

        try {
            return restTemplate.getForObject(url, OpenWeatherMapResponse.class);
        } catch (RestClientException e) {
            logger.error("Error fetching current weather for {}: {}", city, e.getMessage());
            throw new WeatherApiException("Failed to fetch current weather for " + city, e);
        }
    }

    public OpenWeatherMapForecastResponse getForecast(String city) {
        String url = UriComponentsBuilder
                .fromUriString(apiUrl + "/forecast")
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", units)
                .build()
                .toUriString();

        logger.debug("Fetching forecast for {} from: {}", city, url.replace(apiKey, "API_KEY"));

        try {
            return restTemplate.getForObject(url, OpenWeatherMapForecastResponse.class);
        } catch (RestClientException e) {
            logger.error("Error fetching forecast for {}: {}", city, e.getMessage());
            throw new WeatherApiException("Failed to fetch forecast for " + city, e);
        }
    }
}
