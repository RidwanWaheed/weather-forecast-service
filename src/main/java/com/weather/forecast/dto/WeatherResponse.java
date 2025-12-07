package com.weather.forecast.dto;

import com.weather.forecast.model.WeatherCondition;

import java.math.BigDecimal;
import java.time.Instant;

public record WeatherResponse(
        String city,
        String country,
        Instant timestamp,
        BigDecimal temperature,
        Integer humidity,
        BigDecimal windSpeed,
        Integer windDirection,
        Integer pressure,
        WeatherCondition conditions,
        String description,
        Instant sunrise,
        Instant sunset
) {
}
