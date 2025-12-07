package com.weather.forecast.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @EqualsAndHashCode.Include
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
