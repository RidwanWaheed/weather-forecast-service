package com.weather.forecast.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "current_weather")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "city_id", nullable = false)
    private  City city;

    private LocalDateTime timestamp;

    private Double temperature;

    private Integer humidity;

    private Double windSpeed;

    private Integer windDirection;

    private Integer pressure;

    private  String weatherMain;

    private LocalDateTime sunset;

    private LocalDateTime sunrise;

    private LocalDateTime lastUpdate;
}
