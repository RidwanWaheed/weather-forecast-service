package com.weather.forecast.util;

import com.weather.forecast.dto.ForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapForecastResponse;
import com.weather.forecast.dto.OpenWeatherMapResponse;
import com.weather.forecast.dto.WeatherResponse;
import com.weather.forecast.model.City;
import com.weather.forecast.model.CurrentWeather;
import com.weather.forecast.model.Forecast;
import com.weather.forecast.service.CityService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeatherMapper {

    private final CityService cityService;

    public WeatherMapper(CityService cityService) {
        this.cityService = cityService;
    }

    public boolean isDataFresh(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        // Data is fresh if it's less than 30 minutes old
        return timestamp.isAfter(LocalDateTime.now().minusMinutes(30));
    }

    public void updateCityFromResponse(City city, OpenWeatherMapResponse response) {
        if (response.getSys() != null) {
            city.setCountry(response.getSys().getCountry());
        }
        if (response.getCoord() != null) {
            city.setLatitude(response.getCoord().getLat());
            city.setLongitude(response.getCoord().getLon());
        }
        cityService.saveCity(city);
    }

    public void updateCityFromResponse(City city, OpenWeatherMapForecastResponse response) {
        if (response.getCity() != null) {
            city.setCountry(response.getCity().getCountry());

            if (response.getCity().getCoord() != null) {
                city.setLatitude(response.getCity().getCoord().getLat());
                city.setLongitude(response.getCity().getCoord().getLon());
            }
        }
        cityService.saveCity(city);
    }

    public CurrentWeather mapToCurrentWeather(City city, OpenWeatherMapResponse response) {
        CurrentWeather weather = new CurrentWeather();

        weather.setCity(city);
        weather.setTimestamp(
                LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(response.getDt()),
                        ZoneId.systemDefault()
                )
        );

        if (response.getMain() != null) {
            weather.setTemperature(response.getMain().getTemp());
            weather.setHumidity(response.getMain().getHumidity());
            weather.setPressure(response.getMain().getPressure());
        }

        if (response.getWind() != null) {
            weather.setWindSpeed(response.getWind().getSpeed());
            weather.setWindDirection(response.getWind().getDeg());
        }

        if (response.getWeather() != null && !response.getWeather().isEmpty()) {
            weather.setWeatherMain(response.getWeather().get(0).getMain());
            weather.setWeatherDescription(response.getWeather().get(0).getDescription());
        }

        if (response.getSys() != null) {
            weather.setSunrise(
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(response.getSys().getSunrise()),
                            ZoneId.systemDefault()
                    )
            );
            weather.setSunset(
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(response.getSys().getSunset()),
                            ZoneId.systemDefault()
                    )
            );
        }

        weather.setLastUpdated(LocalDateTime.now());

        return weather;
    }

    public List<Forecast> mapToForecasts(City city, OpenWeatherMapForecastResponse response) {
        List<Forecast> forecasts = new ArrayList<>();

        if (response.getList() != null) {
            for (OpenWeatherMapForecastResponse.ForecastItem item : response.getList()) {
                Forecast forecast = new Forecast();
                forecast.setCity(city);

                // Parse datetime
                forecast.setForecastDate(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(item.getDt()),
                                ZoneId.systemDefault()
                        )
                );

                if (item.getMain() != null) {
                    forecast.setTemperature(item.getMain().getTemp());
                    forecast.setHumidity(item.getMain().getHumidity());
                    forecast.setPressure(item.getMain().getPressure());
                }

                if (item.getWind() != null) {
                    forecast.setWindSpeed(item.getWind().getSpeed());
                    forecast.setWindDirection(item.getWind().getDeg());
                }

                if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                    forecast.setWeatherMain(item.getWeather().get(0).getMain());
                    forecast.setWeatherDescription(item.getWeather().get(0).getDescription());
                }

                if (item.getRain() != null) {
                    forecast.setRainVolume(item.getRain().getThreeHour());
                }

                forecast.setProbability(item.getPop());

                forecasts.add(forecast);
            }
        }

        return forecasts;
    }

    public WeatherResponse mapToWeatherResponse(CurrentWeather weather) {
        return WeatherResponse.builder()
                .city(weather.getCity().getName())
                .country(weather.getCity().getCountry())
                .timestamp(weather.getTimestamp())
                .temperature(weather.getTemperature())
                .humidity(weather.getHumidity())
                .windSpeed(weather.getWindSpeed())
                .windDirection(weather.getWindDirection())
                .pressure(weather.getPressure())
                .conditions(weather.getWeatherMain())
                .description(weather.getWeatherDescription())
                .sunrise(weather.getSunrise())
                .sunset(weather.getSunset())
                .build();
    }

    public ForecastResponse mapToForecastResponse(City city, List<Forecast> forecasts) {
        List<ForecastResponse.ForecastItem> forecastItems = forecasts.stream()
                .map(forecast -> ForecastResponse.ForecastItem.builder()
                        .date(forecast.getForecastDate())
                        .temperature(forecast.getTemperature())
                        .humidity(forecast.getHumidity())
                        .windSpeed(forecast.getWindSpeed())
                        .conditions(forecast.getWeatherMain())
                        .description(forecast.getWeatherDescription())
                        .rainVolume(forecast.getRainVolume())
                        .probability(forecast.getProbability())
                        .build())
                .collect(Collectors.toList());

        return ForecastResponse.builder()
                .city(city.getName())
                .country(city.getCountry())
                .forecasts(forecastItems)
                .build();
    }
}