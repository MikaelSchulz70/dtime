-- DTime Database Initialization Script
-- This script sets up the initial database structure

-- Create the main database (already created by ENV vars, but good to document)
-- The database 'dtime' is created automatically by the POSTGRES_DB environment variable

-- Create extensions that might be useful
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schema for application
CREATE SCHEMA IF NOT EXISTS dtime;

-- Set search path
ALTER DATABASE dtime SET search_path TO dtime, public;

-- Create user roles if they don't exist
DO $$
BEGIN
    -- Create read-only role for reporting
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'dtime_readonly') THEN
        CREATE ROLE dtime_readonly;
        GRANT CONNECT ON DATABASE dtime TO dtime_readonly;
        GRANT USAGE ON SCHEMA dtime TO dtime_readonly;
        GRANT SELECT ON ALL TABLES IN SCHEMA dtime TO dtime_readonly;
        ALTER DEFAULT PRIVILEGES IN SCHEMA dtime GRANT SELECT ON TABLES TO dtime_readonly;
    END IF;

    -- Create application role
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'dtime_app') THEN
        CREATE ROLE dtime_app;
        GRANT CONNECT ON DATABASE dtime TO dtime_app;
        GRANT USAGE, CREATE ON SCHEMA dtime TO dtime_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA dtime TO dtime_app;
        GRANT USAGE ON ALL SEQUENCES IN SCHEMA dtime TO dtime_app;
        ALTER DEFAULT PRIVILEGES IN SCHEMA dtime GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO dtime_app;
        ALTER DEFAULT PRIVILEGES IN SCHEMA dtime GRANT USAGE ON SEQUENCES TO dtime_app;
    END IF;
END
$$;

-- Grant the application user the app role
GRANT dtime_app TO dtime;

-- Set timezone for the database
SET timezone = 'UTC';

-- Log the initialization
INSERT INTO pg_stat_statements_info VALUES ('Database initialized for DTime application') 
ON CONFLICT DO NOTHING;