package com.weather.forecast.repository;

import com.weather.forecast.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    List<Forecast> findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
            Long cityId, LocalDateTime date);
}
