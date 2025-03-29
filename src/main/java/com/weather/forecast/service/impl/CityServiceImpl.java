package com.weather.forecast.service.impl;

import com.weather.forecast.model.City;
import com.weather.forecast.repository.CityRepository;
import com.weather.forecast.service.CityService;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CityServiceImpl implements CityService {

    private CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional
    public City findOrCreateCity(String cityName) {
        return findByName(cityName)
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setName(cityName);
                    newCity.setSearchCount(1);
                    newCity.setLastSearched(LocalDateTime.now());
                    return cityRepository.save(newCity);
                });
    }

    @Override
    @Cacheable(value = "citySearch", key = "#cityName.toLowerCase()")
    public Optional<City> findByName(String cityName) {
        return cityRepository.findByNameIgnoreCase(cityName);
    }

    @Override
    public List<City> getRecentlySearchedCities(int limit) {
        return cityRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "lastSearched"))
        ).getContent();
    }

    @Override
    public List<City> getFrequentlySearchedCities(int limit) {
        return cityRepository.findTopSearchedCities(limit);
    }

    @Override
    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    @Override
    public void incrementSearchCount(City city) {
        city.incrementSearchCount();
        cityRepository.save(city);
    }
}
