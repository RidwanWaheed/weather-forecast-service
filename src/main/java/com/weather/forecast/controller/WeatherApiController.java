package com.weather.forecast.controller;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import com.weather.forecast.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "Weather data endpoints")
@Validated
public class WeatherApiController {

    private final WeatherService weatherService;

    public WeatherApiController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    @Operation(summary = "Get current weather", description = "Returns current weather conditions for a city")
    @ApiResponse(responseCode = "200", description = "Weather data retrieved",
            content = @Content(schema = @Schema(implementation = WeatherResponse.class)))
    @ApiResponse(responseCode = "404", description = "City not found")
    @ApiResponse(responseCode = "503", description = "Weather service unavailable")
    public ResponseEntity<?> getCurrentWeather(
            @Parameter(description = "City name", example = "London")
            @RequestParam @NotBlank String city) {
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
    @Operation(summary = "Get weather forecast", description = "Returns 5-day weather forecast for a city")
    @ApiResponse(responseCode = "200", description = "Forecast data retrieved",
            content = @Content(schema = @Schema(implementation = ForecastResponse.class)))
    @ApiResponse(responseCode = "404", description = "City not found")
    @ApiResponse(responseCode = "503", description = "Weather service unavailable")
    public ResponseEntity<?> getForecast(
            @Parameter(description = "City name", example = "London")
            @RequestParam @NotBlank String city) {
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ConstraintViolationException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", e);
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
