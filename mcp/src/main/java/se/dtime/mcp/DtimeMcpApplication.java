package se.dtime.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * dtime-mcp: Spring AI MCP server (streamable HTTP) exposing read-only tools backed by dtime REST.
 * Authenticates to the backend with Authentik OAuth2 client_credentials ({@code Authorization: Bearer}).
 */
@Slf4j
@SpringBootApplication
public class DtimeMcpApplication {

    public static void main(String[] args) {
        log.info("Starting dtime-mcp application");
        SpringApplication.run(DtimeMcpApplication.class, args);
        log.info("dtime-mcp application started successfully");
    }
}

