package com.weather.forecast.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
        testWeatherResponse = WeatherResponse.builder()
                .city("London")
                .country("GB")
                .temperature(20.0)
                .humidity(65)
                .windSpeed(5.2)
                .windDirection(180)
                .pressure(1012)
                .conditions("Partly Cloudy")
                .description("scattered clouds")
                .timestamp(LocalDateTime.now())
                .sunrise(LocalDateTime.now().withHour(6).withMinute(12))
                .sunset(LocalDateTime.now().withHour(18).withMinute(34))
                .build();

        ForecastResponse.ForecastItem forecastItem1 = ForecastResponse.ForecastItem.builder()
                .date(LocalDateTime.now().plusDays(1))
                .temperature(22.0)
                .humidity(60)
                .windSpeed(4.8)
                .conditions("Sunny")
                .description("clear sky")
                .probability(0.1)
                .build();

        ForecastResponse.ForecastItem forecastItem2 = ForecastResponse.ForecastItem.builder()
                .date(LocalDateTime.now().plusDays(2))
                .temperature(18.0)
                .humidity(70)
                .windSpeed(6.2)
                .conditions("Rain")
                .description("light rain")
                .probability(0.8)
                .rainVolume(2.5)
                .build();

        testForecastResponse = ForecastResponse.builder()
                .city("London")
                .country("GB")
                .forecasts(Arrays.asList(forecastItem1, forecastItem2))
                .build();
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
                .andExpect(jsonPath("$.temperature").value(20.0))
                .andExpect(jsonPath("$.humidity").value(65))
                .andExpect(jsonPath("$.windSpeed").value(5.2))
                .andExpect(jsonPath("$.pressure").value(1012))
                .andExpect(jsonPath("$.conditions").value("Partly Cloudy"))
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
                .andExpect(jsonPath("$.forecasts[0].temperature").value(22.0))
                .andExpect(jsonPath("$.forecasts[0].conditions").value("Sunny"))
                .andExpect(jsonPath("$.forecasts[1].temperature").value(18.0))
                .andExpect(jsonPath("$.forecasts[1].conditions").value("Rain"))
                .andExpect(jsonPath("$.forecasts[1].rainVolume").value(2.5));
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
    void getCurrentWeather_WithEmptyStringCity_ShouldStillCallService() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("")).thenReturn(testWeatherResponse);

        // When & Then
        mockMvc.perform(get("/api/weather/current")
                        .param("city", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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