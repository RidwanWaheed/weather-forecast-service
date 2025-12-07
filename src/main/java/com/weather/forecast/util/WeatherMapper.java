package com.weather.forecast.util;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
import com.weather.forecast.model.Forecast;
import com.weather.forecast.model.WeatherCondition;
import com.weather.forecast.repository.CurrentWeatherRepository;
import com.weather.forecast.service.CityService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Converts between OpenWeatherMap API responses and domain entities. */
@Component
public class WeatherMapper {

    private final CityService cityService;
    private final CurrentWeatherRepository currentWeatherRepository;

    public WeatherMapper(CityService cityService, CurrentWeatherRepository currentWeatherRepository) {
        this.cityService = cityService;
        this.currentWeatherRepository = currentWeatherRepository;
    }

    /** @return true if timestamp is within the last 30 minutes */
    public boolean isDataFresh(Instant timestamp) {
        if (timestamp == null) {
            return false;
        }
        return timestamp.isAfter(Instant.now().minus(30, ChronoUnit.MINUTES));
    }

    public void updateCityFromResponse(City city, OpenWeatherMapResponse response) {
        if (response.sys() != null) {
            city.setCountry(response.sys().country());
        }
        if (response.coord() != null) {
            city.setLatitude(response.coord().lat());
            city.setLongitude(response.coord().lon());
        }
        cityService.saveCity(city);
    }

    public void updateCityFromResponse(City city, OpenWeatherMapForecastResponse response) {
        if (response.city() != null) {
            city.setCountry(response.city().country());

            if (response.city().coord() != null) {
                city.setLatitude(response.city().coord().lat());
                city.setLongitude(response.city().coord().lon());
            }
        }
        cityService.saveCity(city);
    }

    public CurrentWeather mapToCurrentWeather(City city, OpenWeatherMapResponse response) {
        CurrentWeather weather = currentWeatherRepository.findByCityId(city.getId()).orElse(new CurrentWeather());

        weather.setCity(city);
        weather.setTimestamp(Instant.ofEpochSecond(response.dt()));

        if (response.main() != null) {
            weather.setTemperature(toBigDecimal(response.main().temp()));
            weather.setHumidity(response.main().humidity());
            weather.setPressure(response.main().pressure());
        }

        if (response.wind() != null) {
            weather.setWindSpeed(toBigDecimal(response.wind().speed()));
            weather.setWindDirection(response.wind().deg());
        }

        if (response.weather() != null && !response.weather().isEmpty()) {
            weather.setWeatherMain(WeatherCondition.fromString(response.weather().getFirst().main()));
            weather.setWeatherDescription(response.weather().getFirst().description());
        }

        if (response.sys() != null) {
            weather.setSunrise(Instant.ofEpochSecond(response.sys().sunrise()));
            weather.setSunset(Instant.ofEpochSecond(response.sys().sunset()));
        }

        weather.setLastUpdated(Instant.now());

        return weather;
    }

    public List<Forecast> mapToForecasts(City city, OpenWeatherMapForecastResponse response) {
        List<Forecast> forecasts = new ArrayList<>();

        if (response.list() != null) {
            for (OpenWeatherMapForecastResponse.ForecastItem item : response.list()) {
                Forecast forecast = new Forecast();
                forecast.setCity(city);

                forecast.setForecastDate(Instant.ofEpochSecond(item.dt()));

                if (item.main() != null) {
                    forecast.setTemperature(toBigDecimal(item.main().temp()));
                    forecast.setHumidity(item.main().humidity());
                    forecast.setPressure(item.main().pressure());
                }

                if (item.wind() != null) {
                    forecast.setWindSpeed(toBigDecimal(item.wind().speed()));
                    forecast.setWindDirection(item.wind().deg());
                }

                if (item.weather() != null && !item.weather().isEmpty()) {
                    forecast.setWeatherMain(WeatherCondition.fromString(item.weather().getFirst().main()));
                    forecast.setWeatherDescription(item.weather().getFirst().description());
                }

                if (item.rain() != null) {
                    forecast.setRainVolume(toBigDecimal(item.rain().threeHour()));
                }

                forecast.setProbability(toBigDecimal(item.pop()));

                forecasts.add(forecast);
            }
        }

        return forecasts;
    }

    public WeatherResponse mapToWeatherResponse(CurrentWeather weather) {
        return new WeatherResponse(
                weather.getCity().getName(),
                weather.getCity().getCountry(),
                weather.getTimestamp(),
                weather.getTemperature(),
                weather.getHumidity(),
                weather.getWindSpeed(),
                weather.getWindDirection(),
                weather.getPressure(),
                weather.getWeatherMain(),
                weather.getWeatherDescription(),
                weather.getSunrise(),
                weather.getSunset()
        );
    }

    public ForecastResponse mapToForecastResponse(City city, List<Forecast> forecasts) {
        List<ForecastResponse.ForecastItem> forecastItems = forecasts.stream()
                .map(forecast -> new ForecastResponse.ForecastItem(
                        forecast.getForecastDate(),
                        forecast.getTemperature(),
                        forecast.getHumidity(),
                        forecast.getWindSpeed(),
                        forecast.getWeatherMain(),
                        forecast.getWeatherDescription(),
                        forecast.getRainVolume(),
                        forecast.getProbability()
                ))
                .toList();

        return new ForecastResponse(
                city.getName(),
                city.getCountry(),
                forecastItems
        );
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}
