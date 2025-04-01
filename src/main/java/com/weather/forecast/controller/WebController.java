package com.weather.forecast.controller;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.model.City;
import com.weather.forecast.service.CityService;
import com.weather.forecast.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {

    private final WeatherService weatherService;
    private final CityService cityService;

    public WebController(WeatherService weatherService, CityService cityService) {
        this.weatherService = weatherService;
        this.cityService = cityService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Get recently searched cities for the home page
        model.addAttribute("cities", cityService.getFrequentlySearchedCities(5));
        return "home";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam(required = false) String city, Model model) {
        if (city != null && !city.trim().isEmpty()) {
            // Get current weather
            WeatherResponse currentWeather = weatherService.getCurrentWeather(city);
            model.addAttribute("weather", currentWeather);

            // Get forecast
            ForecastResponse forecast = weatherService.getForecast(city);
            model.addAttribute("forecast", forecast);

            // Get recently searched cities
            List<City> recentCities = cityService.getRecentlySearchedCities(5);
            model.addAttribute("recentCities", recentCities);

            return "weather";
        }
        return "redirect:/";
    }

    @PostMapping("/search")
    public String search(@RequestParam String city) {
        return "redirect:/weather?city=" + city;
    }
}
