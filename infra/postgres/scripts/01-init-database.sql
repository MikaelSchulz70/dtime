-- Create required databases when container initializes first time
SELECT 'CREATE DATABASE dtime'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dtime')\gexec

SELECT 'CREATE DATABASE authentik'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'authentik')\gexec

-- Configure DTime database objects
\connect dtime

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE SCHEMA IF NOT EXISTS dtime;
ALTER DATABASE dtime SET search_path TO dtime, public;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'dtime_readonly') THEN
        CREATE ROLE dtime_readonly;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'dtime_app') THEN
        CREATE ROLE dtime_app;
    END IF;
END
$$;

GRANT CONNECT ON DATABASE dtime TO dtime_readonly;
GRANT USAGE ON SCHEMA dtime TO dtime_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA dtime TO dtime_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA dtime GRANT SELECT ON TABLES TO dtime_readonly;

GRANT CONNECT ON DATABASE dtime TO dtime_app;
GRANT USAGE, CREATE ON SCHEMA dtime TO dtime_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA dtime TO dtime_app;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA dtime TO dtime_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA dtime GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO dtime_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA dtime GRANT USAGE ON SEQUENCES TO dtime_app;

SET timezone = 'UTC';
