.PHONY: help build test run clean up down dev logs db

COMPOSE := docker-compose -f docker/docker-compose.yml
COMPOSE_DEV := docker-compose -f docker/docker-compose.dev.yml

help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Local:"
	@echo "  build   Build the application"
	@echo "  test    Run tests"
	@echo "  run     Run locally"
	@echo "  clean   Clean build artifacts"
	@echo ""
	@echo "Docker:"
	@echo "  up      Start containers"
	@echo "  down    Stop containers"
	@echo "  dev     Start dev containers"
	@echo "  logs    View logs"
	@echo "  db      Database shell"

build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

run:
	./mvnw spring-boot:run

clean:
	./mvnw clean
	$(COMPOSE) down -v
	$(COMPOSE_DEV) down -v

up:
	$(COMPOSE) up --build -d

down:
	$(COMPOSE) down
	$(COMPOSE_DEV) down

dev:
	$(COMPOSE_DEV) up --build

logs:
	$(COMPOSE) logs -f app

db:
	$(COMPOSE) exec db psql -U weather_user -d weatherdb
