package com.weather.forecast.service;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.model.City;

public interface WeatherService {

    WeatherResponse getCurrentWeather(String cityName);

    ForecastResponse getForecast(String cityName);

    void refreshWeatherData(City city);
}