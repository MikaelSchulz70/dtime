package se.dtime.repository;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for all Repository integration tests.
 * Provides common configuration for H2 database setup and JPA testing.
 */
@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true;INIT=CREATE SCHEMA IF NOT EXISTS \"public\"",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC"
})
public abstract class BaseRepositoryIT {
    // This base class provides common test configuration for all Repository integration tests
}