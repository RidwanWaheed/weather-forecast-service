package com.weather.forecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Maps to OpenWeatherMap 5-day forecast API response. */
public record OpenWeatherMapForecastResponse(
        String cod,
        Integer message,
        Integer cnt,
        List<ForecastItem> list,
        City city
) {
    public record ForecastItem(
            Long dt,
            Main main,
            List<OpenWeatherMapResponse.Weather> weather,
            OpenWeatherMapResponse.Clouds clouds,
            OpenWeatherMapResponse.Wind wind,
            Integer visibility,
            Double pop,
            Rain rain,
            Snow snow,
            @JsonProperty("dt_txt") String dtTxt
    ) {
        public record Rain(
                @JsonProperty("3h") Double threeHour
        ) {
        }

        public record Snow(
                @JsonProperty("3h") Double threeHour
        ) {
        }
    }

    public record Main(
            Double temp,
            @JsonProperty("feels_like") Double feelsLike,
            @JsonProperty("temp_min") Double tempMin,
            @JsonProperty("temp_max") Double tempMax,
            Integer pressure,
            @JsonProperty("sea_level") Integer seaLevel,
            @JsonProperty("grnd_level") Integer groundLevel,
            Integer humidity,
            @JsonProperty("temp_kf") Double tempKf
    ) {
    }

    public record City(
            Long id,
            String name,
            OpenWeatherMapResponse.Coord coord,
            String country,
            Integer population,
            Integer timezone,
            Long sunrise,
            Long sunset
    ) {
    }
}
