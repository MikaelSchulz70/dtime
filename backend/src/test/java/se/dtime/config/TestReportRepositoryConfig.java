package se.dtime.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import se.dtime.repository.jdbc.ReportRepository;

import javax.sql.DataSource;

@TestConfiguration
public class TestReportRepositoryConfig {

    @Bean
    @Primary
    public ReportRepository testReportRepository(DataSource dataSource) {
        ReportRepository repository = new ReportRepository(dataSource);
        repository.setDataSource(dataSource);
        return repository;
    }
}