package com.weather.forecast.service.impl;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
import com.weather.forecast.model.Forecast;
import com.weather.forecast.repository.CurrentWeatherRepository;
import com.weather.forecast.repository.ForecastRepository;
import com.weather.forecast.service.CityService;
import com.weather.forecast.service.OpenWeatherMapClient;
import com.weather.forecast.util.WeatherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private OpenWeatherMapClient weatherClient;

    @Mock
    private CityService cityService;

    @Mock
    private CurrentWeatherRepository currentWeatherRepository;

    @Mock
    private ForecastRepository forecastRepository;

    @Mock
    private WeatherMapper weatherMapper;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private City testCity;
    private CurrentWeather testCurrentWeather;
    private OpenWeatherMapResponse testApiResponse;
    private WeatherResponse testWeatherResponse;

    @BeforeEach
    void setUp() {
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("London");
        testCity.setCountry("GB");

        testCurrentWeather = new CurrentWeather();
        testCurrentWeather.setId(1L);
        testCurrentWeather.setCity(testCity);
        testCurrentWeather.setTemperature(20.0);
        testCurrentWeather.setLastUpdated(LocalDateTime.now().minusMinutes(10));

        testApiResponse = new OpenWeatherMapResponse();
        testApiResponse.setName("London");

        testWeatherResponse = WeatherResponse.builder()
                .city("London")
                .country("GB")
                .temperature(20.0)
                .build();
    }

    @Test
    void getCurrentWeather_WhenFreshDataExists_ShouldReturnCachedData() {
        // Given
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.isDataFresh(any(LocalDateTime.class))).thenReturn(true);
        when(weatherMapper.mapToWeatherResponse(testCurrentWeather)).thenReturn(testWeatherResponse);

        // When
        WeatherResponse result = weatherService.getCurrentWeather("London");

        // Then
        assertNotNull(result);
        assertEquals("London", result.getCity());
        verify(cityService).incrementSearchCount(testCity);
        verify(weatherClient, never()).getCurrentWeather(anyString());
    }

    @Test
    void getCurrentWeather_WhenDataIsStale_ShouldFetchFromAPI() {
        // Given
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.isDataFresh(any(LocalDateTime.class))).thenReturn(false);
        when(weatherClient.getCurrentWeather("London")).thenReturn(testApiResponse);
        when(weatherMapper.mapToCurrentWeather(testCity, testApiResponse)).thenReturn(testCurrentWeather);
        when(currentWeatherRepository.save(testCurrentWeather)).thenReturn(testCurrentWeather);
        when(weatherMapper.mapToWeatherResponse(testCurrentWeather)).thenReturn(testWeatherResponse);

        // When
        WeatherResponse result = weatherService.getCurrentWeather("London");

        // Then
        assertNotNull(result);
        assertEquals("London", result.getCity());
        verify(weatherClient).getCurrentWeather("London");
        verify(weatherMapper).updateCityFromResponse(testCity, testApiResponse);
        verify(currentWeatherRepository).save(testCurrentWeather);
    }

    @Test
    void getCurrentWeather_WhenAPIFails_ShouldReturnCachedDataIfAvailable() {
        // Given
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.isDataFresh(any(LocalDateTime.class))).thenReturn(false);
        when(weatherClient.getCurrentWeather("London")).thenThrow(new WeatherApiException("API Error"));
        when(cityService.findByName("London")).thenReturn(Optional.of(testCity));
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.mapToWeatherResponse(testCurrentWeather)).thenReturn(testWeatherResponse);

        // When
        WeatherResponse result = weatherService.getCurrentWeather("London");

        // Then
        assertNotNull(result);
        assertEquals("London", result.getCity());
    }

    @Test
    void getCurrentWeather_WhenAPIFailsAndNoCachedData_ShouldThrowException() {
        // Given
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.empty());
        when(weatherClient.getCurrentWeather("London")).thenThrow(new WeatherApiException("API Error"));
        when(cityService.findByName("London")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(WeatherApiException.class, () -> weatherService.getCurrentWeather("London"));
    }

    @Test
    void getForecast_WhenFreshDataExists_ShouldReturnCachedData() {
        // Given
        List<Forecast> testForecasts = Arrays.asList(new Forecast(), new Forecast());
        ForecastResponse testForecastResponse = ForecastResponse.builder()
                .city("London")
                .country("GB")
                .build();

        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(testForecasts);
        when(weatherMapper.isDataFresh(any(LocalDateTime.class))).thenReturn(true);
        when(weatherMapper.mapToForecastResponse(testCity, testForecasts)).thenReturn(testForecastResponse);

        // When
        ForecastResponse result = weatherService.getForecast("London");

        // Then
        assertNotNull(result);
        assertEquals("London", result.getCity());
        verify(weatherClient, never()).getForecast(anyString());
    }

    @Test
    void getForecast_WhenDataIsStale_ShouldFetchFromAPI() {
        // Given
        OpenWeatherMapForecastResponse apiResponse = new OpenWeatherMapForecastResponse();
        List<Forecast> newForecasts = Arrays.asList(new Forecast(), new Forecast());
        ForecastResponse testForecastResponse = ForecastResponse.builder()
                .city("London")
                .country("GB")
                .build();

        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(weatherClient.getForecast("London")).thenReturn(apiResponse);
        // Fix: Use doNothing for void method
        doNothing().when(forecastRepository).deleteByCityId(1L);
        when(weatherMapper.mapToForecasts(testCity, apiResponse)).thenReturn(newForecasts);
        when(forecastRepository.saveAll(newForecasts)).thenReturn(newForecasts);
        when(weatherMapper.mapToForecastResponse(testCity, newForecasts)).thenReturn(testForecastResponse);

        // When
        ForecastResponse result = weatherService.getForecast("London");

        // Then
        assertNotNull(result);
        assertEquals("London", result.getCity());
        verify(weatherClient).getForecast("London");
        verify(forecastRepository).deleteByCityId(1L);
        verify(forecastRepository).saveAll(newForecasts);
    }

    @Test
    void refreshWeatherData_ShouldUpdateBothCurrentAndForecastData() {
        // Given
        OpenWeatherMapResponse currentResponse = new OpenWeatherMapResponse();
        OpenWeatherMapForecastResponse forecastResponse = new OpenWeatherMapForecastResponse();
        List<Forecast> forecasts = Arrays.asList(new Forecast());

        when(weatherClient.getCurrentWeather("London")).thenReturn(currentResponse);
        when(weatherClient.getForecast("London")).thenReturn(forecastResponse);
        when(weatherMapper.mapToCurrentWeather(testCity, currentResponse)).thenReturn(testCurrentWeather);
        when(currentWeatherRepository.save(testCurrentWeather)).thenReturn(testCurrentWeather);
        when(weatherMapper.mapToForecasts(testCity, forecastResponse)).thenReturn(forecasts);
        when(forecastRepository.saveAll(forecasts)).thenReturn(forecasts);
        // Fix: Use doNothing for void method
        doNothing().when(forecastRepository).deleteByCityId(1L);

        // When
        weatherService.refreshWeatherData(testCity);

        // Then
        verify(weatherClient).getCurrentWeather("London");
        verify(weatherClient).getForecast("London");
        verify(currentWeatherRepository).save(testCurrentWeather);
        verify(forecastRepository).deleteByCityId(1L);
        verify(forecastRepository).saveAll(forecasts);
    }

    @Test
    void refreshWeatherData_WhenAPIFails_ShouldLogErrorAndContinue() {
        // Given
        when(weatherClient.getCurrentWeather("London")).thenThrow(new WeatherApiException("API Error"));

        // When
        assertDoesNotThrow(() -> weatherService.refreshWeatherData(testCity));

        // Then
        verify(weatherClient).getCurrentWeather("London");
        verify(currentWeatherRepository, never()).save(any());
    }
}