package se.dtime.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import se.dtime.repository.jdbc.ReportRepository;

@TestConfiguration
public class TestReportRepositoryConfig {

    @Bean
    @Primary
    public ReportRepository testReportRepository(JdbcTemplate jdbcTemplate) {
        return new ReportRepository(jdbcTemplate);
    }
}