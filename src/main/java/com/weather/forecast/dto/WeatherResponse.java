package com.weather.forecast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private String city;
    private String country;
    private LocalDateTime timestamp;
    private Double temperature;
    private Integer humidity;
    private Double windSpeed;
    private Integer windDirection;
    private Integer pressure;
    private String conditions;
    private String description;
    private LocalDateTime sunrise;
    private LocalDateTime sunset;
}
