-- Create the public schema for H2 test database
CREATE SCHEMA IF NOT EXISTS PUBLIC;

-- Create sequences needed by the entities
CREATE SEQUENCE IF NOT EXISTS seq_users START WITH 2 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_account START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_closedate START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_monthlycheck START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_participation START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_systemproperty START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_publicholiday START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_task START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_timeentry START WITH 1 INCREMENT BY 1;

-- Create rate table (optional table referenced in FollowUpReportRepository)
CREATE TABLE IF NOT EXISTS rate (
    id BIGINT PRIMARY KEY,
    id_task_contributor BIGINT,
    fromdate DATE,
    todate DATE,
    comment VARCHAR(255)
);

-- Note: monthlycheck table will be created automatically by Hibernate based on MonthlyCheckPO entity