package com.weather.forecast.repository;

import com.weather.forecast.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    @Query("SELECT f FROM Forecast f JOIN FETCH f.city c WHERE c.id = :cityId AND f.forecastDate > :date ORDER BY f.forecastDate ASC")
    List<Forecast> findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
            @Param("cityId") Long cityId, @Param("date") Instant date);

    @Modifying
    @Transactional
    @Query("DELETE FROM Forecast f WHERE f.city.id = :cityId")
    void deleteByCityId(@Param("cityId") Long cityId);
}