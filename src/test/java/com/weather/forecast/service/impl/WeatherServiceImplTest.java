package com.weather.forecast.service.impl;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
import com.weather.forecast.model.Forecast;
import com.weather.forecast.model.WeatherCondition;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        testCurrentWeather.setTemperature(new BigDecimal("20.00"));
        testCurrentWeather.setLastUpdated(Instant.now().minus(10, ChronoUnit.MINUTES));

        testApiResponse = new OpenWeatherMapResponse(
                null, null, null, null, null, null, null, null, null,
                System.currentTimeMillis() / 1000, null, null, 1L, "London", 200
        );

        testWeatherResponse = new WeatherResponse(
                "London", "GB", Instant.now(), new BigDecimal("20.00"),
                50, new BigDecimal("5.00"), 180, 1013,
                WeatherCondition.CLEAR, "Clear sky", Instant.now(), Instant.now()
        );
    }

    @Test
    void getCurrentWeather_WhenFreshDataExists_ShouldReturnCachedData() {
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.isDataFresh(any(Instant.class))).thenReturn(true);
        when(weatherMapper.mapToWeatherResponse(testCurrentWeather)).thenReturn(testWeatherResponse);

        WeatherResponse result = weatherService.getCurrentWeather("London");

        assertNotNull(result);
        assertEquals("London", result.city());
        verify(cityService).incrementSearchCount(testCity);
        verify(weatherClient, never()).getCurrentWeather(anyString());
    }

    @Test
    void getCurrentWeather_WhenDataIsStale_ShouldFetchFromAPI() {
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.isDataFresh(any(Instant.class))).thenReturn(false);
        when(weatherClient.getCurrentWeather("London")).thenReturn(testApiResponse);
        when(weatherMapper.mapToCurrentWeather(testCity, testApiResponse)).thenReturn(testCurrentWeather);
        when(currentWeatherRepository.save(testCurrentWeather)).thenReturn(testCurrentWeather);
        when(weatherMapper.mapToWeatherResponse(testCurrentWeather)).thenReturn(testWeatherResponse);

        WeatherResponse result = weatherService.getCurrentWeather("London");

        assertNotNull(result);
        assertEquals("London", result.city());
        verify(weatherClient).getCurrentWeather("London");
        verify(weatherMapper).updateCityFromResponse(testCity, testApiResponse);
        verify(currentWeatherRepository).save(testCurrentWeather);
    }

    @Test
    void getCurrentWeather_WhenAPIFails_ShouldReturnCachedDataIfAvailable() {
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.of(testCurrentWeather));
        when(weatherMapper.isDataFresh(any(Instant.class))).thenReturn(false);
        when(weatherClient.getCurrentWeather("London")).thenThrow(new WeatherApiException("API Error"));
        when(cityService.findByName("London")).thenReturn(Optional.of(testCity));
        when(weatherMapper.mapToWeatherResponse(testCurrentWeather)).thenReturn(testWeatherResponse);

        WeatherResponse result = weatherService.getCurrentWeather("London");

        assertNotNull(result);
        assertEquals("London", result.city());
    }

    @Test
    void getCurrentWeather_WhenAPIFailsAndNoCachedData_ShouldThrowException() {
        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(currentWeatherRepository.findByCityId(1L)).thenReturn(Optional.empty());
        when(weatherClient.getCurrentWeather("London")).thenThrow(new WeatherApiException("API Error"));
        when(cityService.findByName("London")).thenReturn(Optional.empty());

        assertThrows(WeatherApiException.class, () -> weatherService.getCurrentWeather("London"));
    }

    @Test
    void getForecast_WhenFreshDataExists_ShouldReturnCachedData() {
        Forecast forecast1 = new Forecast();
        forecast1.setForecastDate(Instant.now().plus(1, ChronoUnit.DAYS));
        forecast1.setTemperature(new BigDecimal("22.00"));

        Forecast forecast2 = new Forecast();
        forecast2.setForecastDate(Instant.now().plus(2, ChronoUnit.DAYS));
        forecast2.setTemperature(new BigDecimal("25.00"));

        List<Forecast> testForecasts = Arrays.asList(forecast1, forecast2);

        ForecastResponse testForecastResponse = new ForecastResponse("London", "GB", List.of());

        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(eq(1L), any(Instant.class)))
                .thenReturn(testForecasts);
        when(weatherMapper.isDataFresh(any(Instant.class))).thenReturn(true);
        when(weatherMapper.mapToForecastResponse(testCity, testForecasts)).thenReturn(testForecastResponse);

        ForecastResponse result = weatherService.getForecast("London");

        assertNotNull(result);
        assertEquals("London", result.city());
        verify(weatherClient, never()).getForecast(anyString());
    }

    @Test
    void getForecast_WhenDataIsStale_ShouldFetchFromAPI() {
        OpenWeatherMapForecastResponse apiResponse = new OpenWeatherMapForecastResponse(
                "200", 0, 0, List.of(), null
        );
        List<Forecast> newForecasts = Arrays.asList(new Forecast(), new Forecast());
        ForecastResponse testForecastResponse = new ForecastResponse("London", "GB", List.of());

        when(cityService.findOrCreateCity("London")).thenReturn(testCity);
        when(forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(eq(1L), any(Instant.class)))
                .thenReturn(Arrays.asList());
        when(weatherClient.getForecast("London")).thenReturn(apiResponse);
        doNothing().when(forecastRepository).deleteByCityId(1L);
        when(weatherMapper.mapToForecasts(testCity, apiResponse)).thenReturn(newForecasts);
        when(forecastRepository.saveAll(newForecasts)).thenReturn(newForecasts);
        when(weatherMapper.mapToForecastResponse(testCity, newForecasts)).thenReturn(testForecastResponse);

        ForecastResponse result = weatherService.getForecast("London");

        assertNotNull(result);
        assertEquals("London", result.city());
        verify(weatherClient).getForecast("London");
        verify(forecastRepository).deleteByCityId(1L);
        verify(forecastRepository).saveAll(newForecasts);
    }

    @Test
    void refreshWeatherData_ShouldUpdateBothCurrentAndForecastData() {
        OpenWeatherMapResponse currentResponse = new OpenWeatherMapResponse(
                null, null, null, null, null, null, null, null, null,
                System.currentTimeMillis() / 1000, null, null, 1L, "London", 200
        );
        OpenWeatherMapForecastResponse forecastResponse = new OpenWeatherMapForecastResponse(
                "200", 0, 0, List.of(), null
        );
        List<Forecast> forecasts = Arrays.asList(new Forecast());

        when(weatherClient.getCurrentWeather("London")).thenReturn(currentResponse);
        when(weatherClient.getForecast("London")).thenReturn(forecastResponse);
        when(weatherMapper.mapToCurrentWeather(testCity, currentResponse)).thenReturn(testCurrentWeather);
        when(currentWeatherRepository.save(testCurrentWeather)).thenReturn(testCurrentWeather);
        when(weatherMapper.mapToForecasts(testCity, forecastResponse)).thenReturn(forecasts);
        when(forecastRepository.saveAll(forecasts)).thenReturn(forecasts);
        doNothing().when(forecastRepository).deleteByCityId(1L);

        weatherService.refreshWeatherData(testCity);

        verify(weatherClient).getCurrentWeather("London");
        verify(weatherClient).getForecast("London");
        verify(currentWeatherRepository).save(testCurrentWeather);
        verify(forecastRepository).deleteByCityId(1L);
        verify(forecastRepository).saveAll(forecasts);
    }

    @Test
    void refreshWeatherData_WhenAPIFails_ShouldLogErrorAndContinue() {
        when(weatherClient.getCurrentWeather("London")).thenThrow(new WeatherApiException("API Error"));

        assertDoesNotThrow(() -> weatherService.refreshWeatherData(testCity));

        verify(weatherClient).getCurrentWeather("London");
        verify(currentWeatherRepository, never()).save(any());
    }
}
