package se.dtime.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.dtime.mcp.client.BackendApiClient;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks Authentik and dtime-backend reachability; retries on a fixed delay while degraded.
 */
@Slf4j
@Component
public class BackendConnectivityMonitor {

    private final BackendApiClient backendApiClient;

    private final AtomicBoolean authentikReachable = new AtomicBoolean(false);
    private final AtomicBoolean backendReachable = new AtomicBoolean(false);
    private final AtomicReference<Instant> lastSuccessfulBackendCheck = new AtomicReference<>();

    public BackendConnectivityMonitor(BackendApiClient backendApiClient) {
        this.backendApiClient = backendApiClient;
    }

    /** Runs connectivity probes and updates state; logs transitions. */
    public ConnectivitySnapshot checkNow() {
        boolean authOk = backendApiClient.canObtainAccessToken();
        boolean backendOk = authOk && backendApiClient.canAccessBackendApi();

        updateAuthentikState(authOk);
        updateBackendState(backendOk);

        if (backendOk) {
            lastSuccessfulBackendCheck.set(Instant.now());
        }

        return snapshot();
    }

    @Scheduled(fixedDelayString = "${mcp.backend.connectivity-check-interval-ms:30000}")
    void scheduledCheck() {
        checkNow();
    }

    public ConnectivitySnapshot snapshot() {
        return new ConnectivitySnapshot(
                authentikReachable.get(),
                backendReachable.get(),
                lastSuccessfulBackendCheck.get());
    }

    public boolean isFullyConnected() {
        ConnectivitySnapshot s = snapshot();
        return s.authentikReachable() && s.backendReachable();
    }

    private void updateAuthentikState(boolean ok) {
        boolean wasOk = authentikReachable.getAndSet(ok);
        if (ok && !wasOk) {
            log.info("dtime-mcp: Authentik connectivity restored");
        } else if (!ok && wasOk) {
            log.warn("dtime-mcp: Authentik connectivity lost");
        }
    }

    private void updateBackendState(boolean ok) {
        boolean wasOk = backendReachable.getAndSet(ok);
        if (ok && !wasOk) {
            log.info("dtime-mcp: Backend API connectivity restored");
        } else if (!ok && wasOk) {
            log.warn("dtime-mcp: Backend API connectivity lost");
        }
    }

    public record ConnectivitySnapshot(
            boolean authentikReachable,
            boolean backendReachable,
            Instant lastSuccessfulBackendCheck) {

        public String authentikStatus() {
            return authentikReachable ? "ok" : "unavailable";
        }

        public String backendStatus() {
            return backendReachable ? "ok" : "unavailable";
        }
    }
}
