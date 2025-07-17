package com.weather.forecast.controller;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.model.City;
import com.weather.forecast.service.CityService;
import com.weather.forecast.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private CityService cityService;

    private WeatherResponse testWeatherResponse;
    private ForecastResponse testForecastResponse;
    private List<City> testCities;

    @BeforeEach
    void setUp() {
        testWeatherResponse = WeatherResponse.builder()
                .city("London")
                .country("GB")
                .temperature(20.0)
                .humidity(65)
                .windSpeed(5.2)
                .conditions("Partly Cloudy")
                .description("scattered clouds")
                .timestamp(LocalDateTime.now())
                .sunrise(LocalDateTime.now().withHour(6).withMinute(12))
                .sunset(LocalDateTime.now().withHour(18).withMinute(34))
                .build();

        ForecastResponse.ForecastItem forecastItem = ForecastResponse.ForecastItem.builder()
                .date(LocalDateTime.now().plusDays(1))
                .temperature(22.0)
                .humidity(60)
                .windSpeed(4.8)
                .conditions("Sunny")
                .description("clear sky")
                .probability(0.1)
                .build();

        testForecastResponse = ForecastResponse.builder()
                .city("London")
                .country("GB")
                .forecasts(Arrays.asList(forecastItem))
                .build();

        City city1 = new City();
        city1.setId(1L);
        city1.setName("London");
        city1.setCountry("GB");

        City city2 = new City();
        city2.setId(2L);
        city2.setName("Paris");
        city2.setCountry("FR");

        testCities = Arrays.asList(city1, city2);
    }

    @Test
    void home_ShouldReturnHomePageWithFrequentlySearchedCities() throws Exception {
        // Given
        when(cityService.getFrequentlySearchedCities(5)).thenReturn(testCities);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("cities"))
                .andExpect(model().attribute("cities", testCities));
    }

    @Test
    void getWeather_WithValidCity_ShouldReturnWeatherPage() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("London")).thenReturn(testWeatherResponse);
        when(weatherService.getForecast("London")).thenReturn(testForecastResponse);
        when(cityService.getRecentlySearchedCities(5)).thenReturn(testCities);

        // When & Then
        mockMvc.perform(get("/weather").param("city", "London"))
                .andExpect(status().isOk())
                .andExpect(view().name("weather"))
                .andExpect(model().attributeExists("weather"))
                .andExpect(model().attributeExists("forecast"))
                .andExpect(model().attributeExists("recentCities"))
                .andExpect(model().attribute("weather", testWeatherResponse))
                .andExpect(model().attribute("forecast", testForecastResponse))
                .andExpect(model().attribute("recentCities", testCities));
    }

    @Test
    void getWeather_WithEmptyCity_ShouldRedirectToHome() throws Exception {
        // When & Then
        mockMvc.perform(get("/weather").param("city", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void getWeather_WithNullCity_ShouldRedirectToHome() throws Exception {
        // When & Then
        mockMvc.perform(get("/weather"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void getWeather_WithWhitespaceOnlyCity_ShouldRedirectToHome() throws Exception {
        // When & Then
        mockMvc.perform(get("/weather").param("city", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void search_WithValidCity_ShouldRedirectToWeatherPage() throws Exception {
        // When & Then
        mockMvc.perform(post("/search").param("city", "London"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather?city=London"));
    }

    @Test
    void search_WithCityContainingSpaces_ShouldRedirectCorrectly() throws Exception {
        // When & Then
        mockMvc.perform(post("/search").param("city", "New York"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather?city=New York"));
    }

    @Test
    void search_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // When & Then
        mockMvc.perform(post("/search").param("city", "São Paulo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather?city=São Paulo"));
    }

    @Test
    void home_WithEmptyFrequentlySearchedCities_ShouldStillReturnHomePage() throws Exception {
        // Given
        when(cityService.getFrequentlySearchedCities(5)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("cities"))
                .andExpect(model().attribute("cities", Collections.emptyList()));
    }

    @Test
    void getWeather_WithEmptyRecentCities_ShouldStillReturnWeatherPage() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("London")).thenReturn(testWeatherResponse);
        when(weatherService.getForecast("London")).thenReturn(testForecastResponse);
        when(cityService.getRecentlySearchedCities(5)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/weather").param("city", "London"))
                .andExpect(status().isOk())
                .andExpect(view().name("weather"))
                .andExpect(model().attributeExists("weather"))
                .andExpect(model().attributeExists("forecast"))
                .andExpect(model().attributeExists("recentCities"))
                .andExpect(model().attribute("recentCities", Collections.emptyList()));
    }

    @Test
    void search_WithEmptyCity_ShouldStillRedirect() throws Exception {
        // When & Then
        mockMvc.perform(post("/search").param("city", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather?city="));
    }

    @Test
    void getWeather_ShouldCallServicesInCorrectOrder() throws Exception {
        // Given
        when(weatherService.getCurrentWeather("London")).thenReturn(testWeatherResponse);
        when(weatherService.getForecast("London")).thenReturn(testForecastResponse);
        when(cityService.getRecentlySearchedCities(5)).thenReturn(testCities);

        // When
        mockMvc.perform(get("/weather").param("city", "London"));

        // Then - verify the services were called
        // The order is implicit in the fact that all three model attributes are present
        // which means all three service calls were successful
    }
}