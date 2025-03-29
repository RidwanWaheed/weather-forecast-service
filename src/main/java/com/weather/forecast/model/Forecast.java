package com.weather.forecast.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "forecasts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    private LocalDateTime forecastDate;

    private Double temperature;

    private Double windSpeed;

    private Integer windDirection;

    private Integer pressure;

    private Integer humidity;

    private String weatherMain;

    private String weatherDescription;

    private Double rainVolume;

    private Double probability;
}
