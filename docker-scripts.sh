#!/bin/bash
# Docker helper scripts for weather forecast application

# Production deployment
function docker_prod() {
    echo "Starting production environment..."
    docker-compose down
    docker-compose up --build -d
    echo "Production environment started!"
    echo "Application: http://localhost:8080"
    echo "API Documentation: http://localhost:8080/swagger-ui.html"
}

# Development environment
function docker_dev() {
    echo "Starting development environment..."
    docker-compose -f docker-compose.dev.yml down
    docker-compose -f docker-compose.dev.yml up --build
    echo "Development environment started!"
    echo "Application: http://localhost:8080"
    echo "Debug port: 5005"
    echo "Database: localhost:5433"
}

# Stop all containers
function docker_stop() {
    echo "Stopping all containers..."
    docker-compose down
    docker-compose -f docker-compose.dev.yml down
    echo "All containers stopped!"
}

# Clean up everything (containers, volumes, images)
function docker_clean() {
    echo "Cleaning up Docker resources..."
    docker-compose down -v
    docker-compose -f docker-compose.dev.yml down -v
    docker system prune -f
    docker volume prune -f
    echo "Cleanup completed!"
}

# View logs
function docker_logs() {
    local service=${1:-app}
    echo "Showing logs for service: $service"
    docker-compose logs -f $service
}

# Database shell
function docker_db() {
    echo "Connecting to PostgreSQL database..."
    docker-compose exec db psql -U weather_user -d weatherdb
}

# Application shell
function docker_shell() {
    echo "Connecting to application container..."
    docker-compose exec app sh
}

# Show help
function docker_help() {
    echo "Weather Forecast Application - Docker Helper"
    echo "Usage: source docker-scripts.sh && <function_name>"
    echo ""
    echo "Available functions:"
    echo "  docker_prod     - Start production environment"
    echo "  docker_dev      - Start development environment"
    echo "  docker_stop     - Stop all containers"
    echo "  docker_clean    - Clean up all Docker resources"
    echo "  docker_logs     - View application logs"
    echo "  docker_db       - Connect to database shell"
    echo "  docker_shell    - Connect to application shell"
    echo "  docker_help     - Show this help message"
    echo ""
    echo "Examples:"
    echo "  docker_prod"
    echo "  docker_logs app"
    echo "  docker_logs db"
}

# If script is run directly, show help
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    docker_help
fi