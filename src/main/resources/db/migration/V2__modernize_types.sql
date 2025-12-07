-- Migration to modernize data types for better precision and timezone handling

-- Cities table: Change timestamp to use timezone
ALTER TABLE cities
    ALTER COLUMN last_searched TYPE TIMESTAMP WITH TIME ZONE;

-- Current weather table changes
ALTER TABLE current_weather
    ALTER COLUMN timestamp TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN temperature TYPE DECIMAL(5, 2),
    ALTER COLUMN wind_speed TYPE DECIMAL(5, 2),
    ALTER COLUMN sunset TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN sunrise TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN last_updated TYPE TIMESTAMP WITH TIME ZONE;

-- Forecasts table changes
ALTER TABLE forecasts
    ALTER COLUMN forecast_date TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN temperature TYPE DECIMAL(5, 2),
    ALTER COLUMN wind_speed TYPE DECIMAL(5, 2),
    ALTER COLUMN rain_volume TYPE DECIMAL(6, 2),
    ALTER COLUMN probability TYPE DECIMAL(3, 2);
