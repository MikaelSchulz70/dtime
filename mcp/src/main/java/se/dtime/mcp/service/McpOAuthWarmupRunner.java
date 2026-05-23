package se.dtime.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.dtime.mcp.config.BackendApiConfig;

/**
 * Validates OAuth configuration at startup and probes Authentik/backend connectivity.
 * Unreachable dependencies start in degraded mode with background retries unless strict mode is enabled.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mcp.backend.validate-on-startup", havingValue = "true", matchIfMissing = true)
public class McpOAuthWarmupRunner implements ApplicationRunner {

    private final BackendApiConfig backendApiConfig;
    private final BackendConnectivityMonitor connectivityMonitor;

    public McpOAuthWarmupRunner(BackendApiConfig backendApiConfig, BackendConnectivityMonitor connectivityMonitor) {
        this.backendApiConfig = backendApiConfig;
        this.connectivityMonitor = connectivityMonitor;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (backendApiConfig.isFailOnMissingOauthConfig() && !backendApiConfig.isOAuthConfigPresent()) {
            throw new IllegalStateException(
                    "dtime-mcp: OAuth configuration incomplete — set MCP_OAUTH_TOKEN_URI, MCP_OAUTH_CLIENT_ID, "
                            + "and MCP_OAUTH_CLIENT_SECRET");
        }

        BackendConnectivityMonitor.ConnectivitySnapshot snapshot = connectivityMonitor.checkNow();

        if (snapshot.authentikReachable()) {
            log.info("dtime-mcp: Authentik client_credentials token acquisition OK");
        }
        if (snapshot.backendReachable()) {
            log.info("dtime-mcp: Backend Bearer API access OK");
        }

        if (connectivityMonitor.isFullyConnected()) {
            return;
        }

        String message = buildDegradedMessage(snapshot);
        if (backendApiConfig.isFailOnUnavailable()) {
            throw new IllegalStateException(message);
        }

        log.warn(
                "{} Retrying every {}ms.",
                message,
                backendApiConfig.getConnectivityCheckIntervalMs());
    }

    private static String buildDegradedMessage(BackendConnectivityMonitor.ConnectivitySnapshot snapshot) {
        if (!snapshot.authentikReachable()) {
            return "dtime-mcp started in degraded mode: Authentik unreachable — check MCP_OAUTH_TOKEN_URI and "
                    + "MCP_OAUTH_CLIENT_ID/SECRET.";
        }
        return "dtime-mcp started in degraded mode: dtime-backend unreachable or rejected Bearer token — "
                + "ensure backend is up and OAUTH_AUTHENTIK_MACHINE_JWT_* is configured.";
    }
}
