package com.weather.forecast.repository;

import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
import com.weather.forecast.model.Forecast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CurrentWeatherRepository currentWeatherRepository;

    @Autowired
    private ForecastRepository forecastRepository;

    private City testCity1;
    private City testCity2;

    @BeforeEach
    void setUp() {
        testCity1 = new City();
        testCity1.setName("London");
        testCity1.setCountry("GB");
        testCity1.setSearchCount(10);
        testCity1.setLastSearched(LocalDateTime.now().minusDays(1));

        testCity2 = new City();
        testCity2.setName("Paris");
        testCity2.setCountry("FR");
        testCity2.setSearchCount(5);
        testCity2.setLastSearched(LocalDateTime.now().minusDays(2));

        entityManager.persistAndFlush(testCity1);
        entityManager.persistAndFlush(testCity2);
    }

    @Test
    void cityRepository_findByNameIgnoreCase_ShouldFindCityRegardlessOfCase() {
        // When
        Optional<City> resultLowerCase = cityRepository.findByNameIgnoreCase("london");
        Optional<City> resultUpperCase = cityRepository.findByNameIgnoreCase("LONDON");
        Optional<City> resultMixedCase = cityRepository.findByNameIgnoreCase("LoNdOn");

        // Then
        assertTrue(resultLowerCase.isPresent());
        assertTrue(resultUpperCase.isPresent());
        assertTrue(resultMixedCase.isPresent());

        assertEquals("London", resultLowerCase.get().getName());
        assertEquals("London", resultUpperCase.get().getName());
        assertEquals("London", resultMixedCase.get().getName());
    }

    @Test
    void cityRepository_findByNameIgnoreCase_ShouldReturnEmptyForNonExistentCity() {
        // When
        Optional<City> result = cityRepository.findByNameIgnoreCase("NonExistentCity");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void cityRepository_findTopSearchedCities_ShouldReturnCitiesOrderedBySearchCount() {
        // When
        List<City> result = cityRepository.findTopSearchedCities(10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("London", result.get(0).getName()); // Higher search count
        assertEquals("Paris", result.get(1).getName());   // Lower search count
    }

    @Test
    void cityRepository_findTopSearchedCities_ShouldRespectLimit() {
        // When
        List<City> result = cityRepository.findTopSearchedCities(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("London", result.get(0).getName());
    }

    @Test
    void currentWeatherRepository_findByCityId_ShouldReturnWeatherForCity() {
        // Given
        CurrentWeather weather = new CurrentWeather();
        weather.setCity(testCity1);
        weather.setTemperature(20.0);
        weather.setHumidity(65);
        weather.setTimestamp(LocalDateTime.now());
        entityManager.persistAndFlush(weather);

        // When
        Optional<CurrentWeather> result = currentWeatherRepository.findByCityId(testCity1.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCity1.getId(), result.get().getCity().getId());
        assertEquals(20.0, result.get().getTemperature());
        assertEquals(65, result.get().getHumidity());
    }

    @Test
    void currentWeatherRepository_findByCityId_ShouldReturnEmptyForNonExistentWeather() {
        // When
        Optional<CurrentWeather> result = currentWeatherRepository.findByCityId(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void forecastRepository_findByCityIdAndForecastDateGreaterThan_ShouldReturnFutureForecasts() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Forecast pastForecast = new Forecast();
        pastForecast.setCity(testCity1);
        pastForecast.setForecastDate(now.minusDays(1));
        pastForecast.setTemperature(18.0);

        Forecast futureForecast1 = new Forecast();
        futureForecast1.setCity(testCity1);
        futureForecast1.setForecastDate(now.plusDays(1));
        futureForecast1.setTemperature(22.0);

        Forecast futureForecast2 = new Forecast();
        futureForecast2.setCity(testCity1);
        futureForecast2.setForecastDate(now.plusDays(2));
        futureForecast2.setTemperature(25.0);

        entityManager.persistAndFlush(pastForecast);
        entityManager.persistAndFlush(futureForecast1);
        entityManager.persistAndFlush(futureForecast2);

        // When
        List<Forecast> result = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
                testCity1.getId(), now);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Only future forecasts
        assertEquals(22.0, result.get(0).getTemperature()); // Earlier future forecast first
        assertEquals(25.0, result.get(1).getTemperature()); // Later future forecast second
        assertTrue(result.get(0).getForecastDate().isBefore(result.get(1).getForecastDate()));
    }

    @Test
    void forecastRepository_findByCityIdAndForecastDateGreaterThan_ShouldReturnEmptyForNoFutureForecasts() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Forecast pastForecast = new Forecast();
        pastForecast.setCity(testCity1);
        pastForecast.setForecastDate(now.minusDays(1));
        pastForecast.setTemperature(18.0);

        entityManager.persistAndFlush(pastForecast);

        // When
        List<Forecast> result = forecastRepository.findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
                testCity1.getId(), now);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void forecastRepository_deleteByCityId_ShouldDeleteAllForecastsForCity() {
        // Given
        Forecast forecast1 = new Forecast();
        forecast1.setCity(testCity1);
        forecast1.setForecastDate(LocalDateTime.now().plusDays(1));
        forecast1.setTemperature(20.0);

        Forecast forecast2 = new Forecast();
        forecast2.setCity(testCity1);
        forecast2.setForecastDate(LocalDateTime.now().plusDays(2));
        forecast2.setTemperature(22.0);

        Forecast forecast3 = new Forecast();
        forecast3.setCity(testCity2);
        forecast3.setForecastDate(LocalDateTime.now().plusDays(1));
        forecast3.setTemperature(18.0);

        entityManager.persistAndFlush(forecast1);
        entityManager.persistAndFlush(forecast2);
        entityManager.persistAndFlush(forecast3);

        // When
        forecastRepository.deleteByCityId(testCity1.getId());
        entityManager.flush();

        // Then
        List<Forecast> remainingForecasts = forecastRepository.findAll();
        assertEquals(1, remainingForecasts.size());
        assertEquals(testCity2.getId(), remainingForecasts.get(0).getCity().getId());
    }

    @Test
    void forecastRepository_deleteByCityId_ShouldHandleNonExistentCityId() {
        // Given
        Forecast forecast = new Forecast();
        forecast.setCity(testCity1);
        forecast.setForecastDate(LocalDateTime.now().plusDays(1));
        forecast.setTemperature(20.0);
        entityManager.persistAndFlush(forecast);

        // When
        forecastRepository.deleteByCityId(999L); // Non-existent city ID
        entityManager.flush();

        // Then
        List<Forecast> remainingForecasts = forecastRepository.findAll();
        assertEquals(1, remainingForecasts.size()); // Original forecast should still exist
    }

    @Test
    void currentWeatherRepository_saveAndRetrieve_ShouldWorkCorrectly() {
        // Given
        CurrentWeather weather = new CurrentWeather();
        weather.setCity(testCity1);
        weather.setTemperature(15.5);
        weather.setHumidity(80);
        weather.setWindSpeed(3.2);
        weather.setWindDirection(270);
        weather.setPressure(1015);
        weather.setWeatherMain("Rain");
        weather.setWeatherDescription("light rain");
        weather.setTimestamp(LocalDateTime.now());
        weather.setSunrise(LocalDateTime.now().withHour(6).withMinute(30));
        weather.setSunset(LocalDateTime.now().withHour(19).withMinute(45));
        weather.setLastUpdated(LocalDateTime.now());

        // When
        CurrentWeather saved = currentWeatherRepository.save(weather);
        Optional<CurrentWeather> retrieved = currentWeatherRepository.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        CurrentWeather result = retrieved.get();
        assertEquals(15.5, result.getTemperature());
        assertEquals(80, result.getHumidity());
        assertEquals(3.2, result.getWindSpeed());
        assertEquals(270, result.getWindDirection());
        assertEquals(1015, result.getPressure());
        assertEquals("Rain", result.getWeatherMain());
        assertEquals("light rain", result.getWeatherDescription());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getSunrise());
        assertNotNull(result.getSunset());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    void cityRepository_saveAndUpdate_ShouldWorkCorrectly() {
        // Given
        City newCity = new City();
        newCity.setName("Berlin");
        newCity.setCountry("DE");
        newCity.setLatitude(52.5200);
        newCity.setLongitude(13.4050);
        newCity.setSearchCount(1);
        newCity.setLastSearched(LocalDateTime.now());

        // When
        City saved = cityRepository.save(newCity);

        // Update the city
        saved.setSearchCount(3);
        saved.incrementSearchCount();
        City updated = cityRepository.save(saved);

        // Then
        assertNotNull(updated.getId());
        assertEquals("Berlin", updated.getName());
        assertEquals("DE", updated.getCountry());
        assertEquals(52.5200, updated.getLatitude());
        assertEquals(13.4050, updated.getLongitude());
        assertEquals(4, updated.getSearchCount()); // 3 + 1 from increment
        assertNotNull(updated.getLastSearched());
    }
}