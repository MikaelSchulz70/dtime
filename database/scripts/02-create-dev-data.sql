-- Development Data Setup Script
-- This script is only for development environment
-- It creates sample data for testing

-- Note: The actual table schema is managed by Liquibase in the Spring Boot application
-- This script only provides sample data once tables are created

-- The Spring Boot application will run Liquibase migrations automatically
-- which will create the actual table structure from:
-- backend/src/main/resources/db/changelog/

-- You can add sample data here after the application has run its migrations
-- For example:

-- Sample users (passwords should be BCrypt hashed in real application)
/*
INSERT INTO users (username, email, password_hash, role, created_date) VALUES
('admin', 'admin@dtime.local', '$2a$10$example_bcrypt_hash', 'ADMIN', NOW()),
('user1', 'user1@dtime.local', '$2a$10$example_bcrypt_hash', 'USER', NOW())
ON CONFLICT (username) DO NOTHING;

-- Sample companies
INSERT INTO companies (name, description, active, created_date) VALUES
('Sample Company', 'Development test company', true, NOW()),
('Client Corp', 'Another test company', true, NOW())
ON CONFLICT (name) DO NOTHING;

-- Sample projects
INSERT INTO projects (name, description, company_id, active, created_date) VALUES
('Project Alpha', 'First test project', 1, true, NOW()),
('Project Beta', 'Second test project', 1, true, NOW())
ON CONFLICT (name) DO NOTHING;
*/

-- This file will be executed after table creation by Liquibase
-- Add your development seed data here when tables are available