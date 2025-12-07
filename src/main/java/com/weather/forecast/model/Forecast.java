package com.weather.forecast.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "forecasts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    private Instant forecastDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeed;

    private Integer windDirection;

    private Integer pressure;

    private Integer humidity;

    @Enumerated(EnumType.STRING)
    private WeatherCondition weatherMain;

    private String weatherDescription;

    @Column(precision = 6, scale = 2)
    private BigDecimal rainVolume;

    @Column(precision = 3, scale = 2)
    private BigDecimal probability;
}
