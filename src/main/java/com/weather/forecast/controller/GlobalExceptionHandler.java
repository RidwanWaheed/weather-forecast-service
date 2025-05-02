package com.weather.forecast.controller;

import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCityNotFoundException(CityNotFoundException e, Model model) {
        logger.error("City not found: ", e);
        model.addAttribute("errorMessage", "We couldn't find the city you were looking for. Please check the spelling and try again.");
        model.addAttribute("showHomeButton", true);
        return "error";
    }

    @ExceptionHandler(WeatherApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleWeatherApiException(WeatherApiException e, Model model) {
        logger.error("Weather API error: ", e);
        model.addAttribute("errorMessage", "We're having trouble getting the latest weather data. Please try again later.");
        model.addAttribute("showHomeButton", true);
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e, Model model) {
        logger.error("Database error: ", e);
        model.addAttribute("errorMessage", "Something went wrong on our end. Our team has been notified.");
        model.addAttribute("showHomeButton", true);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, Model model) {
        logger.error("Unexpected error: ", e);
        model.addAttribute("errorMessage", "Something unexpected happened. Please try again later.");
        model.addAttribute("showHomeButton", true);
        return "error";
    }
}