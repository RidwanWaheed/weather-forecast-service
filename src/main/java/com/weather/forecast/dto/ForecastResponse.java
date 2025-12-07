package com.weather.forecast.dto;

import com.weather.forecast.model.WeatherCondition;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ForecastResponse(
        String city,
        String country,
        List<ForecastItem> forecasts
) {
    public record ForecastItem(
            Instant date,
            BigDecimal temperature,
            Integer humidity,
            BigDecimal windSpeed,
            WeatherCondition conditions,
            String description,
            BigDecimal rainVolume,
            BigDecimal probability
    ) {
    }
}
