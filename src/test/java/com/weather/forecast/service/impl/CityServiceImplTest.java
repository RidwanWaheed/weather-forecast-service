package com.weather.forecast.service.impl;

import com.weather.forecast.model.City;
import com.weather.forecast.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityServiceImpl cityService;

    private City testCity;

    @BeforeEach
    void setUp() {
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("London");
        testCity.setCountry("GB");
        testCity.setSearchCount(5);
        testCity.setLastSearched(Instant.now().minus(1, ChronoUnit.DAYS));
    }

    @Test
    void findOrCreateCity_WhenCityExists_ShouldReturnExistingCity() {
        when(cityRepository.findByNameIgnoreCase("London")).thenReturn(Optional.of(testCity));

        City result = cityService.findOrCreateCity("London");

        assertNotNull(result);
        assertEquals("London", result.getName());
        assertEquals(1L, result.getId());
        verify(cityRepository).findByNameIgnoreCase("London");
        verify(cityRepository, never()).save(any());
    }

    @Test
    void findOrCreateCity_WhenCityDoesNotExist_ShouldCreateNewCity() {
        City newCity = new City();
        newCity.setId(2L);
        newCity.setName("Paris");
        newCity.setSearchCount(1);

        when(cityRepository.findByNameIgnoreCase("Paris")).thenReturn(Optional.empty());
        when(cityRepository.save(any(City.class))).thenReturn(newCity);

        City result = cityService.findOrCreateCity("Paris");

        assertNotNull(result);
        assertEquals("Paris", result.getName());
        assertEquals(2L, result.getId());
        assertEquals(1, result.getSearchCount());
        verify(cityRepository).findByNameIgnoreCase("Paris");
        verify(cityRepository).save(any(City.class));
    }

    @Test
    void findByName_WhenCityExists_ShouldReturnCity() {
        when(cityRepository.findByNameIgnoreCase("London")).thenReturn(Optional.of(testCity));

        Optional<City> result = cityService.findByName("London");

        assertTrue(result.isPresent());
        assertEquals("London", result.get().getName());
        verify(cityRepository).findByNameIgnoreCase("London");
    }

    @Test
    void findByName_WhenCityDoesNotExist_ShouldReturnEmpty() {
        when(cityRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        Optional<City> result = cityService.findByName("NonExistent");

        assertFalse(result.isPresent());
        verify(cityRepository).findByNameIgnoreCase("NonExistent");
    }

    @Test
    void getRecentlySearchedCities_ShouldReturnCitiesOrderedByLastSearched() {
        City city1 = new City();
        city1.setName("London");
        city1.setLastSearched(Instant.now().minus(1, ChronoUnit.DAYS));

        City city2 = new City();
        city2.setName("Paris");
        city2.setLastSearched(Instant.now().minus(2, ChronoUnit.DAYS));

        List<City> cities = Arrays.asList(city1, city2);
        Page<City> cityPage = new PageImpl<>(cities);

        when(cityRepository.findAll(any(PageRequest.class))).thenReturn(cityPage);

        List<City> result = cityService.getRecentlySearchedCities(5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("London", result.get(0).getName());
        assertEquals("Paris", result.get(1).getName());

        verify(cityRepository).findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "lastSearched")));
    }

    @Test
    void getFrequentlySearchedCities_ShouldReturnCitiesOrderedBySearchCount() {
        City city1 = new City();
        city1.setName("London");
        city1.setSearchCount(10);

        City city2 = new City();
        city2.setName("Paris");
        city2.setSearchCount(5);

        List<City> cities = Arrays.asList(city1, city2);

        when(cityRepository.findTopSearchedCities(5)).thenReturn(cities);

        List<City> result = cityService.getFrequentlySearchedCities(5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("London", result.get(0).getName());
        assertEquals("Paris", result.get(1).getName());
        verify(cityRepository).findTopSearchedCities(5);
    }

    @Test
    void saveCity_ShouldReturnSavedCity() {
        when(cityRepository.save(testCity)).thenReturn(testCity);

        City result = cityService.saveCity(testCity);

        assertNotNull(result);
        assertEquals(testCity, result);
        verify(cityRepository).save(testCity);
    }

    @Test
    void incrementSearchCount_ShouldIncrementCountAndUpdateTimestamp() {
        Instant beforeIncrement = Instant.now();
        testCity.setSearchCount(5);
        when(cityRepository.save(testCity)).thenReturn(testCity);

        cityService.incrementSearchCount(testCity);

        assertEquals(6, testCity.getSearchCount());
        assertNotNull(testCity.getLastSearched());
        assertTrue(testCity.getLastSearched().isAfter(beforeIncrement) ||
                testCity.getLastSearched().equals(beforeIncrement));
        verify(cityRepository).save(testCity);
    }

    @Test
    void incrementSearchCount_WhenSearchCountIsNull_ShouldSetToOne() {
        testCity.setSearchCount(null);
        when(cityRepository.save(testCity)).thenReturn(testCity);

        cityService.incrementSearchCount(testCity);

        assertEquals(1, testCity.getSearchCount());
        assertNotNull(testCity.getLastSearched());
        verify(cityRepository).save(testCity);
    }

    @Test
    void findByName_ShouldBeCaseInsensitive() {
        when(cityRepository.findByNameIgnoreCase("london")).thenReturn(Optional.of(testCity));

        Optional<City> result = cityService.findByName("london");

        assertTrue(result.isPresent());
        assertEquals("London", result.get().getName());
        verify(cityRepository).findByNameIgnoreCase("london");
    }
}
