# Weather Forecast Service

A Spring Boot web application that provides weather forecasts for cities using the OpenWeatherMap API.

## Features

- Search weather by city name
- View current weather conditions
- See 5-day weather forecast
- Web dashboard and REST API
- Recently searched cities tracking

## Technologies

- Java 21
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Thymeleaf
- Docker
- Maven

## Quick Start

### Using Docker (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/weather-forecast-service.git
   cd weather-forecast-service
   ```

2. Run with Docker:
   ```bash
   make up
   ```

3. Access the application:
   - Web Interface: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html

### Manual Setup

1. Install PostgreSQL and create database:
   ```sql
   CREATE DATABASE weatherdb;
   CREATE USER weather_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE weatherdb TO weather_user;
   ```

2. Update `application.properties` with your database credentials

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

### Current Weather
```
GET /api/weather/current?city=London
```

### Forecast
```
GET /api/weather/forecast?city=London
```

## Project Structure

```
src/
├── main/java/com/weather/forecast/
│   ├── controller/          # REST controllers and web controllers
│   ├── service/             # Business logic
│   ├── repository/          # Data access layer
│   ├── model/               # JPA entities
│   ├── dto/                 # Data transfer objects
│   └── config/              # Configuration classes
└── main/resources/
    ├── templates/           # Thymeleaf templates
    ├── static/              # CSS, JS files
    └── db/migration/        # Database migration scripts
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENWEATHERMAP_API_KEY` | OpenWeatherMap API key | Test key included |
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:postgresql://localhost:5432/weatherdb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `weather_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Required |

## Development

### Requirements
- Java 21
- Maven 3.6+
- PostgreSQL 12+

### Running Tests
```bash
./mvnw test
```

### Building
```bash
./mvnw clean package
```

## Make Commands

```bash
make build   # Build the application
make test    # Run tests
make run     # Run locally
make clean   # Clean everything

make up      # Start containers
make down    # Stop containers
make dev     # Start dev containers
make logs    # View logs
make db      # Database shell
```

## License

This project is for educational purposes.