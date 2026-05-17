package se.dtime.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.dtime.mcp.client.BackendApiClient;

/**
 * Validates Authentik client_credentials at startup (clear failure if misconfigured).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mcp.backend.validate-on-startup", havingValue = "true", matchIfMissing = true)
public class McpOAuthWarmupRunner implements ApplicationRunner {

    private final BackendApiClient backendApiClient;

    public McpOAuthWarmupRunner(BackendApiClient backendApiClient) {
        this.backendApiClient = backendApiClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!backendApiClient.canObtainAccessToken()) {
            throw new IllegalStateException(
                    "dtime-mcp: Failed to obtain access token — check mcp.backend token-uri and MCP_OAUTH_CLIENT_ID/SECRET");
        }
        log.info("dtime-mcp: Authentik client_credentials token acquisition OK");
        if (!backendApiClient.canAccessBackendApi()) {
            throw new IllegalStateException(
                    "dtime-mcp: Backend rejected Bearer token (401/403). On dtime-backend enable machine JWT and allowlist the MCP client: "
                            + "OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true, "
                            + "OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS=<same as MCP_OAUTH_CLIENT_ID or azp from mcp/scripts/print-access-token-claims.sh>. "
                            + "Restart backend with repo .env loaded (./start-backend.sh), then restart MCP.");
        }
        log.info("dtime-mcp: Backend Bearer API access OK");
    }
}
