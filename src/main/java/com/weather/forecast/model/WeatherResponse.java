package com.weather.forecast.model;

public class WeatherResponse {

    private Main main;
    private Weather[] weather;
    private Wind wind;
    private String name;

    // Getters and Setters


    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }

    public Wind getWind() {
        return wind;
    }


    public String getName() {
        return name;
    }


    public static class Main {
        private double temp;
        private double feels_like;
        private double temp_min;
        private double temp_max;
        private int humidity;

        // Getters and Setters

        public double getTemp() {
            return temp;
        }

        public double getFeels_like() {
            return feels_like;
        }


        public double getTemp_min() {
            return temp_min;
        }

        public double getTemp_max() {
            return temp_max;
        }


        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {
        private String description;

        // Getters and Setters

        public String getDescription() {
            return description;
        }
    }

    public static class Wind {
        private double speed;

        // Getters and Setters
        public double getSpeed() {
            return speed;
        }
    }
}