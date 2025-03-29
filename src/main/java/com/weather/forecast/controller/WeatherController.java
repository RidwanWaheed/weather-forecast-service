//package com.weather.forecast.controller;
//
//
//import com.weather.forecast.dto.WeatherResponse;
//import com.weather.forecast.service.WeatherService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/weather")
//public class WeatherController {
//
//    @Autowired
////    private WeatherService weatherService;
//
//    @GetMapping("/{city}")
//    public String getWeather(@PathVariable String city) {
//        WeatherResponse weatherResponse = weatherService.getWeather(city);
//        return String.format("Weather in %s: %s, Temperature: %.2f°C, Humidity: %d%%, Wind Speed: %.2f m/s",
//                weatherResponse.getName(),
//                weatherResponse.getWeather()[0].getDescription(),
//                weatherResponse.getMain().getTemp() - 273.15, // Convert Kelvin to Celsius
//                weatherResponse.getMain().getHumidity(),
//                weatherResponse.getWind().getSpeed());
//    }
//}