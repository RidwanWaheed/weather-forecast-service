package com.weather.forecast.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.model.WeatherCondition;
import com.weather.forecast.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherApiController.class)
class WeatherApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    private WeatherResponse testWeatherResponse;
    private ForecastResponse testForecastResponse;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        testWeatherResponse = new WeatherResponse(
                "London",
                "GB",
                now,
                new BigDecimal("20.00"),
                65,
                new BigDecimal("5.20"),
                180,
                1012,
                WeatherCondition.CLOUDS,
                "scattered clouds",
                now.minus(6, ChronoUnit.HOURS),
                now.plus(6, ChronoUnit.HOURS)
        );

        ForecastResponse.ForecastItem forecastItem1 = new ForecastResponse.ForecastItem(
                now.plus(1, ChronoUnit.DAYS),
                new BigDecimal("22.00"),
                60,
                new BigDecimal("4.80"),
                WeatherCondition.CLEAR,
                "clear sky",
                null,
                new BigDecimal("0.10")
        );

        ForecastResponse.ForecastItem forecastItem2 = new ForecastResponse.ForecastItem(
                now.plus(2, ChronoUnit.DAYS),
                new BigDecimal("18.00"),
                70,
                new BigDecimal("6.20"),
                WeatherCondition.RAIN,
                "light rain",
                new BigDecimal("2.50"),
                new BigDecimal("0.80")
        );

        testForecastResponse = new ForecastResponse(
                "London",
                "GB",
                Arrays.asList(forecastItem1, forecastItem2)
        );
    }

    @Test
    void getCurrentWeather_WithValidCity_ShouldReturnWeatherData() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("London")).thenReturn(testWeatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.country").value("GB"))
                .andExpect(jsonPath("$.temperature").value(20.00))
                .andExpect(jsonPath("$.humidity").value(65))
                .andExpect(jsonPath("$.windSpeed").value(5.20))
                .andExpect(jsonPath("$.pressure").value(1012))
                .andExpect(jsonPath("$.conditions").value("Clouds"))
                .andExpect(jsonPath("$.description").value("scattered clouds"));
    }

    @Test
    void getCurrentWeather_WithCityNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("InvalidCity"))
                .thenThrow(new CityNotFoundException("City not found: InvalidCity"));

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                        .param("city", "InvalidCity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("City not found"))
                .andExpect(jsonPath("$.details").value("City not found: InvalidCity"));
    }

    @Test
    void getCurrentWeather_WithWeatherApiException_ShouldReturnServiceUnavailable() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("London"))
                .thenThrow(new WeatherApiException("Weather API is unavailable"));

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Weather service unavailable"))
                .andExpect(jsonPath("$.details").value("Weather API is unavailable"));
    }

    @Test
    void getCurrentWeather_WithInternalServerError_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("London"))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.details").value("Unexpected error"));
    }

    @Test
    void getCurrentWeather_WithMissingCityParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/current")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getForecast_WithValidCity_ShouldReturnForecastData() throws Exception {
        // Given
        when(weatherService.getForecast("London")).thenReturn(testForecastResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/forecast")
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.country").value("GB"))
                .andExpect(jsonPath("$.forecasts").isArray())
                .andExpect(jsonPath("$.forecasts").value(hasSize(2)))
                .andExpect(jsonPath("$.forecasts[0].temperature").value(22.00))
                .andExpect(jsonPath("$.forecasts[0].conditions").value("Clear"))
                .andExpect(jsonPath("$.forecasts[1].temperature").value(18.00))
                .andExpect(jsonPath("$.forecasts[1].conditions").value("Rain"))
                .andExpect(jsonPath("$.forecasts[1].rainVolume").value(2.50));
    }

    @Test
    void getForecast_WithCityNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(weatherService.getForecast("InvalidCity"))
                .thenThrow(new CityNotFoundException("City not found: InvalidCity"));

        // When & Then
        mockMvc.perform(get("/api/weather/forecast")
                        .param("city", "InvalidCity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("City not found"));
    }

    @Test
    void getForecast_WithWeatherApiException_ShouldReturnServiceUnavailable() throws Exception {
        // Given
        when(weatherService.getForecast("London"))
                .thenThrow(new WeatherApiException("Weather API is unavailable"));

        // When & Then
        mockMvc.perform(get("/api/weather/forecast")
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Weather service unavailable"));
    }

    @Test
    void getForecast_WithMissingCityParameter_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/weather/forecast")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentWeather_WithEmptyStringCity_ShouldReturnBadRequest() throws Exception {
        // When & Then - Empty city should fail validation
        mockMvc.perform(get("/api/weather/current")
                        .param("city", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getForecast_WithSpecialCharactersInCity_ShouldHandleCorrectly() throws Exception {
        // Given
        String cityWithSpecialChars = "São Paulo";
        when(weatherService.getForecast(cityWithSpecialChars)).thenReturn(testForecastResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/forecast")
                        .param("city", cityWithSpecialChars)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
