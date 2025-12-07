package com.weather.forecast.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

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
    private City city;

    private Instant timestamp;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    private Integer humidity;

    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeed;

    private Integer windDirection;

    private Integer pressure;

    @Enumerated(EnumType.STRING)
    private WeatherCondition weatherMain;

    private String weatherDescription;

    private Instant sunset;

    private Instant sunrise;

    private Instant lastUpdated;
}
