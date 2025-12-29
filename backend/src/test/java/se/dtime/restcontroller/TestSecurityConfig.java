package se.dtime.restcontroller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@TestConfiguration
public class TestSecurityConfig {

    /**
     * Override the production SecurityContextRepository.
     * This allows @WithMockUser to work again in Spring Boot 4.
     */
    @Bean
    @Primary
    public SecurityContextRepository testSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}