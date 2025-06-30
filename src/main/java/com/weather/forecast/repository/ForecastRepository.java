package com.weather.forecast.repository;

import com.weather.forecast.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    /**
     * Finds all forecasts for a given city that are after a specific date,
     * ordering them by date.
     * <p>
     * The {@code JOIN FETCH f.city} clause ensures that the associated City entity
     * is eagerly fetched in the same query, preventing the N+1 select problem.
     *
     * @param cityId The ID of the city.
     * @param date   The date to find forecasts after.
     * @return A list of forecasts with their associated city data.
     */
    @Query("SELECT f FROM Forecast f JOIN FETCH f.city c WHERE c.id = :cityId AND f.forecastDate > :date ORDER BY f.forecastDate ASC")
    List<Forecast> findByCityIdAndForecastDateGreaterThanOrderByForecastDateAsc(
            @Param("cityId") Long cityId, @Param("date") LocalDateTime date);

    /**
     * Deletes all forecast records associated with a specific city ID in a single,
     * efficient bulk operation.
     * <p>
     * The {@code @Modifying} annotation is required for queries that modify data (DELETE, UPDATE).
     * The {@code @Transactional} annotation ensures this operation is executed within a transaction.
     *
     * @param cityId The ID of the city for which to delete forecasts.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Forecast f WHERE f.city.id = :cityId")
    void deleteByCityId(@Param("cityId") Long cityId);
}