package se.dtime.mcp.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;
import se.dtime.mcp.service.BackendConnectivityMonitor;

import java.time.Instant;

/**
 * MCP process health stays UP; exposes Authentik/backend reachability as details.
 */
@Component
public class McpConnectivityHealthIndicator implements HealthIndicator {

    private final BackendConnectivityMonitor connectivityMonitor;

    public McpConnectivityHealthIndicator(BackendConnectivityMonitor connectivityMonitor) {
        this.connectivityMonitor = connectivityMonitor;
    }

    @Override
    public Health health() {
        BackendConnectivityMonitor.ConnectivitySnapshot snapshot = connectivityMonitor.snapshot();
        Health.Builder builder = Health.up()
                .withDetail("authentik", snapshot.authentikStatus())
                .withDetail("backend", snapshot.backendStatus());

        Instant lastOk = snapshot.lastSuccessfulBackendCheck();
        if (lastOk != null) {
            builder.withDetail("lastSuccessfulBackendCheck", lastOk.toString());
        }

        return builder.build();
    }
}
