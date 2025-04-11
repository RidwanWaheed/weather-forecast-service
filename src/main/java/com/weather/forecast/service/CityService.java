package com.weather.forecast.service;

import com.weather.forecast.model.City;

import java.util.List;
import java.util.Optional;

public interface CityService {

    City findOrCreateCity(String cityName);

    Optional<City> findByName(String cityName);

    List<City> getRecentlySearchedCities(int limit);

    List<City> getFrequentlySearchedCities(int limit);

    City saveCity(City city);

    void incrementSearchCount(City city);
}
