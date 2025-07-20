package com.weather.forecast.service;

import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.exception.WeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenWeatherMapClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenWeatherMapClient openWeatherMapClient;

    private String apiUrl;
    private String apiKey;
    private String units;

    @BeforeEach
    void setUp() {
        apiUrl = "https://api.openweathermap.org/data/2.5";
        apiKey = "test-api-key";
        units = "metric";

        // Use reflection to set private fields or create with constructor
        openWeatherMapClient = new OpenWeatherMapClient(restTemplate, apiUrl, apiKey, units);
    }

    @Test
    void getCurrentWeather_WhenSuccessful_ShouldReturnWeatherData() {
        // Given
        String cityName = "London";
        OpenWeatherMapResponse expectedResponse = new OpenWeatherMapResponse();
        expectedResponse.setName("London");

        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapResponse.class)))
                .thenReturn(expectedResponse);

        // When
        OpenWeatherMapResponse result = openWeatherMapClient.getCurrentWeather(cityName);

        // Then
        assertNotNull(result);
        assertEquals("London", result.getName());
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapResponse.class));
    }

    @Test
    void getCurrentWeather_WhenRestClientException_ShouldThrowWeatherApiException() {
        // Given
        String cityName = "InvalidCity";
        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapResponse.class)))
                .thenThrow(new RestClientException("City not found"));

        // When & Then
        WeatherApiException exception = assertThrows(WeatherApiException.class,
                () -> openWeatherMapClient.getCurrentWeather(cityName));

        assertTrue(exception.getMessage().contains("Failed to fetch current weather for InvalidCity"));
        assertNotNull(exception.getCause());
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapResponse.class));
    }

    @Test
    void getForecast_WhenSuccessful_ShouldReturnForecastData() {
        // Given
        String cityName = "London";
        OpenWeatherMapForecastResponse expectedResponse = new OpenWeatherMapForecastResponse();
        OpenWeatherMapForecastResponse.City city = new OpenWeatherMapForecastResponse.City();
        city.setName("London");
        expectedResponse.setCity(city);

        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class)))
                .thenReturn(expectedResponse);

        // When
        OpenWeatherMapForecastResponse result = openWeatherMapClient.getForecast(cityName);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCity());
        assertEquals("London", result.getCity().getName());
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class));
    }

    @Test
    void getForecast_WhenRestClientException_ShouldThrowWeatherApiException() {
        // Given
        String cityName = "InvalidCity";
        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class)))
                .thenThrow(new RestClientException("City not found"));

        // When & Then
        WeatherApiException exception = assertThrows(WeatherApiException.class,
                () -> openWeatherMapClient.getForecast(cityName));

        assertTrue(exception.getMessage().contains("Failed to fetch forecast for InvalidCity"));
        assertNotNull(exception.getCause());
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class));
    }

    @Test
    void getCurrentWeather_ShouldConstructCorrectUrl() {
        // Given
        String cityName = "London";
        OpenWeatherMapResponse response = new OpenWeatherMapResponse();

        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapResponse.class)))
                .thenReturn(response);

        // When
        openWeatherMapClient.getCurrentWeather(cityName);

        // Then - Use ArgumentCaptor to capture the actual URL
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(OpenWeatherMapResponse.class));

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).contains("/weather");
        assertThat(capturedUrl).contains("q=London");
        assertThat(capturedUrl).contains("appid=" + apiKey);
        assertThat(capturedUrl).contains("units=" + units);
    }

    @Test
    void getForecast_ShouldConstructCorrectUrl() {
        // Given
        String cityName = "London";
        OpenWeatherMapForecastResponse response = new OpenWeatherMapForecastResponse();

        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class)))
                .thenReturn(response);

        // When
        openWeatherMapClient.getForecast(cityName);

        // Then - Use ArgumentCaptor to capture the actual URL
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(OpenWeatherMapForecastResponse.class));

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).contains("/forecast");
        assertThat(capturedUrl).contains("q=London");
        assertThat(capturedUrl).contains("appid=" + apiKey);
        assertThat(capturedUrl).contains("units=" + units);
    }

    @Test
    void getCurrentWeather_WithSpecialCharactersInCityName_ShouldHandleCorrectly() {
        // Given
        String cityName = "São Paulo";
        OpenWeatherMapResponse response = new OpenWeatherMapResponse();

        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapResponse.class)))
                .thenReturn(response);

        // When
        openWeatherMapClient.getCurrentWeather(cityName);

        // Then
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapResponse.class));
    }

    @Test
    void getForecast_WithNullResponse_ShouldReturnNull() {
        // Given
        String cityName = "London";
        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class)))
                .thenReturn(null);

        // When
        OpenWeatherMapForecastResponse result = openWeatherMapClient.getForecast(cityName);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapForecastResponse.class));
    }

    @Test
    void getCurrentWeather_WithNullResponse_ShouldReturnNull() {
        // Given
        String cityName = "London";
        when(restTemplate.getForObject(anyString(), eq(OpenWeatherMapResponse.class)))
                .thenReturn(null);

        // When
        OpenWeatherMapResponse result = openWeatherMapClient.getCurrentWeather(cityName);

        // Then
        assertNull(result);
        verify(restTemplate).getForObject(anyString(), eq(OpenWeatherMapResponse.class));
    }
}