package com.weather.forecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Maps to OpenWeatherMap current weather API response. */
public record OpenWeatherMapResponse(
        Coord coord,
        List<Weather> weather,
        String base,
        Main main,
        Integer visibility,
        Wind wind,
        Clouds clouds,
        Rain rain,
        Snow snow,
        Long dt,
        Sys sys,
        Integer timezone,
        Long id,
        String name,
        Integer cod
) {
    public record Coord(
            Double lon,
            Double lat
    ) {
    }

    public record Weather(
            Long id,
            String main,
            String description,
            String icon
    ) {
    }

    public record Main(
            Double temp,
            @JsonProperty("feels_like") Double feelsLike,
            @JsonProperty("temp_min") Double tempMin,
            @JsonProperty("temp_max") Double tempMax,
            Integer pressure,
            Integer humidity,
            @JsonProperty("sea_level") Integer seaLevel,
            @JsonProperty("grnd_level") Integer groundLevel
    ) {
    }

    public record Wind(
            Double speed,
            Integer deg,
            Double gust
    ) {
    }

    public record Clouds(
            Integer all
    ) {
    }

    public record Rain(
            @JsonProperty("1h") Double oneHour,
            @JsonProperty("3h") Double threeHour
    ) {
    }

    public record Snow(
            @JsonProperty("1h") Double oneHour,
            @JsonProperty("3h") Double threeHour
    ) {
    }

    public record Sys(
            Integer type,
            Long id,
            String country,
            Long sunrise,
            Long sunset
    ) {
    }
}
