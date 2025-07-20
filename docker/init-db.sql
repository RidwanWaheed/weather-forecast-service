-- Database initialization script for Docker
-- This script runs when the PostgreSQL container starts for the first time

-- Connect to the weatherdb database
\c weatherdb;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant all privileges to the user
GRANT ALL PRIVILEGES ON DATABASE weatherdb TO weather_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO weather_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO weather_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO weather_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO weather_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO weather_user;

-- Display confirmation
\echo 'Database initialization completed successfully!';
