package com.weather.forecast.controller;

import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackageClasses = WeatherApiController.class)
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCityNotFound(CityNotFoundException e) {
        logger.error("City not found: {}", e.getMessage());
        return errorResponse(HttpStatus.NOT_FOUND, "City not found", e.getMessage());
    }

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<Map<String, Object>> handleWeatherApiError(WeatherApiException e) {
        logger.error("Weather API error: {}", e.getMessage());
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Weather service unavailable", e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(ConstraintViolationException e) {
        logger.error("Validation error: {}", e.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "Validation failed", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception e) {
        logger.error("Unexpected error: ", e);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String error, String details) {
        return new ResponseEntity<>(Map.of(
                "status", status.value(),
                "error", error,
                "details", details != null ? details : "Unknown error"
        ), status);
    }
}
