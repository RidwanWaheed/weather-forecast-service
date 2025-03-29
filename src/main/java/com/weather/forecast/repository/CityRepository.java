package com.weather.forecast.repository;


import com.weather.forecast.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByNameIgnoreCase(String name);

    @Query("SELECT c FROM City c ORDER BY c.searchCount DESC LIMIT ?1")
    List<City> findTopSearchedCities(int limit);
}
