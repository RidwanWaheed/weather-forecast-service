package com.weather.forecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenWeatherMapForecastResponse {
    private String cod;
    private Integer message;
    private Integer cnt;
    private List<ForecastItem> list;
    private City city;

    @Data
    public static class ForecastItem {
        private Long dt;
        private Main main;
        private List<OpenWeatherMapResponse.Weather> weather;
        private OpenWeatherMapResponse.Clouds clouds;
        private OpenWeatherMapResponse.Wind wind;
        private Integer visibility;
        private Double pop;
        private Rain rain;
        private Snow snow;
        @JsonProperty("dt_txt")
        private String dtTxt;

        @Data
        public static class Rain {
            @JsonProperty("3h")
            private Double threeHour;
        }

        @Data
        public static class Snow {
            @JsonProperty("3h")
            private Double threeHour;
        }
    }

    @Data
    public static class Main {
        private Double temp;
        @JsonProperty("feels_like")
        private Double feelsLike;
        @JsonProperty("temp_min")
        private Double tempMin;
        @JsonProperty("temp_max")
        private Double tempMax;
        private Integer pressure;
        @JsonProperty("sea_level")
        private Integer seaLevel;
        @JsonProperty("grnd_level")
        private Integer groundLevel;
        private Integer humidity;
        @JsonProperty("temp_kf")
        private Double tempKf;
    }

    @Data
    public static class City {
        private Long id;
        private String name;
        private OpenWeatherMapResponse.Coord coord;
        private String country;
        private Integer population;
        private Integer timezone;
        private Long sunrise;
        private Long sunset;
    }
}