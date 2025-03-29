package com.weather.forecast.repository;

import com.weather.forecast.model.CurrentWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrentWeatherRepository extends JpaRepository<CurrentWeather, Long> {

    Optional<CurrentWeather> findByCityId(Long cityId);
}
