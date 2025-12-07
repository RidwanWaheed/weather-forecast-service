package com.weather.forecast.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;

    private Double latitude;

    private Double longitude;

    private Instant lastSearched;

    private Integer searchCount = 0;

    public void incrementSearchCount() {
        this.searchCount = (this.searchCount == null) ? 1 : this.searchCount + 1;
        this.lastSearched = Instant.now();
    }
}
