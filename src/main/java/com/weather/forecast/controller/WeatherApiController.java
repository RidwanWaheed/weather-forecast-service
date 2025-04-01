package com.weather.forecast.controller;


import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    private final WeatherService weatherService;

    public WeatherApiController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(@RequestParam String city) {
        try{
            WeatherResponse response = weatherService.getCurrentWeather(city);
            return ResponseEntity.ok(response);
        } catch (CityNotFoundException e){
            return createErrorResponse(HttpStatus.NOT_FOUND, "City not found", e);
        } catch (WeatherApiException e) {
            return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather service unavailable", e);
        } catch (Exception e){
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
        }
    }

    @GetMapping("/forecast")
    public ResponseEntity<?> getForecast(@RequestParam String city) {
        try{
            ForecastResponse response = weatherService.getForecast(city);
            return ResponseEntity.ok(response);
        } catch (CityNotFoundException e){
            return createErrorResponse(HttpStatus.NOT_FOUND, "City not found", e);
        } catch (WeatherApiException e) {
            return createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather service unavailable", e);
        } catch (Exception e){
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, Exception e){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("details", e.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }
}
