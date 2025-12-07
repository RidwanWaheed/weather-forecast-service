package com.weather.forecast.controller;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.service.WeatherService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherApiController {

    private final WeatherService weatherService;

    public WeatherApiController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(@RequestParam @NotBlank String city) {
        try {
            WeatherResponse response = weatherService.getCurrentWeather(city);
            return ResponseEntity.ok(response);
        } catch (CityNotFoundException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "City not found", e);
        } catch (WeatherApiException e) {
            return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather service unavailable", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
        }
    }

    @GetMapping("/forecast")
    public ResponseEntity<?> getForecast(@RequestParam @NotBlank String city) {
        try {
            ForecastResponse response = weatherService.getForecast(city);
            return ResponseEntity.ok(response);
        } catch (CityNotFoundException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "City not found", e);
        } catch (WeatherApiException e) {
            return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather service unavailable", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ConstraintViolationException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Validation failed", e);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message, Exception e) {
        return new ResponseEntity<>(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "details", e.getMessage()
        ), status);
    }
}
