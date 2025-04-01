package com.weather.forecast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {
    private String city;
    private String country;
    private List<ForecastItem> forecasts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastItem {
        private LocalDateTime date;
        private Double temperature;
        private Integer humidity;
        private Double windSpeed;
        private String conditions;
        private String description;
        private Double rainVolume;
        private Double probability;
    }
}