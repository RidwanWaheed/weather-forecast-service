package com.weather.forecast.controller;


import com.weather.forecast.exception.CityNotFoundException;
import com.weather.forecast.exception.WeatherApiException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCityNotFoundException(CityNotFoundException e, Model model){
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @ExceptionHandler(WeatherApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleWeatherApiException(WeatherApiException e, Model model){
        model.addAttribute("errorMessage", "Weather service is temporarily unavailable. Please try again later.");
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, Model model){
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }
}
