# Weather Forecast Service

A Spring Boot application that provides current weather and 5-day forecasts via REST API and web interface, powered by OpenWeatherMap.

## Tech Stack

<p>
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Docker-24-2496ED?logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf&logoColor=white" alt="Thymeleaf" />
</p>

## Architecture

```mermaid
flowchart TB
    subgraph Client
        Browser[Web Browser]
        API[API Client]
    end

    subgraph "Spring Boot"
        WC[WebController]
        AC[WeatherApiController]
        WS[WeatherService]
        Cache[(Caffeine Cache)]
        OWM[OpenWeatherMapClient]
    end

    subgraph Data
        DB[(PostgreSQL)]
    end

    subgraph External
        Weather[OpenWeatherMap API]
    end

    Browser --> WC
    API --> AC
    WC --> WS
    AC --> WS
    WS <--> Cache
    WS <--> DB
    WS --> OWM
    OWM --> Weather
```

## How to Run

```bash
# Clone and configure
git clone https://github.com/RidwanWaheed/weather-forecast-service.git
cd weather-forecast-service
cp .env.example .env
# Edit .env and add your OpenWeatherMap API key

# Run with Docker
make up

# Or run locally (requires PostgreSQL)
./mvnw spring-boot:run
```

Access: http://localhost:8080 | API Docs: http://localhost:8080/swagger-ui.html

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/weather/current?city={name}` | Current weather |
| GET | `/api/weather/forecast?city={name}` | 5-day forecast |

## Make Commands

```bash
make up      # Start containers
make down    # Stop containers
make dev     # Development mode with hot reload
make test    # Run tests
make logs    # View logs
```
