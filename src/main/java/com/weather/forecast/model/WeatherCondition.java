package com.weather.forecast.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Weather condition types matching OpenWeatherMap API values. */
public enum WeatherCondition {
    CLEAR("Clear"),
    CLOUDS("Clouds"),
    RAIN("Rain"),
    DRIZZLE("Drizzle"),
    THUNDERSTORM("Thunderstorm"),
    SNOW("Snow"),
    MIST("Mist"),
    FOG("Fog"),
    HAZE("Haze"),
    DUST("Dust"),
    SAND("Sand"),
    ASH("Ash"),
    SQUALL("Squall"),
    TORNADO("Tornado"),
    SMOKE("Smoke"),
    UNKNOWN("Unknown");

    private final String displayName;

    WeatherCondition(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /** @return matching condition or UNKNOWN if not recognized */
    @JsonCreator
    public static WeatherCondition fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        for (WeatherCondition condition : values()) {
            if (condition.displayName.equalsIgnoreCase(value)) {
                return condition;
            }
        }
        return UNKNOWN;
    }
}
