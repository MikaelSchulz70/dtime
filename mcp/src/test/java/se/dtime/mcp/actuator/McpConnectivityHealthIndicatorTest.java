package se.dtime.mcp.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import se.dtime.mcp.service.BackendConnectivityMonitor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpConnectivityHealthIndicatorTest {

    @Mock
    private BackendConnectivityMonitor connectivityMonitor;

    @InjectMocks
    private McpConnectivityHealthIndicator indicator;

    @Test
    void health_staysUpWithUnavailableDetails() {
        when(connectivityMonitor.snapshot()).thenReturn(
                new BackendConnectivityMonitor.ConnectivitySnapshot(false, false, null));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("authentik", "unavailable");
        assertThat(health.getDetails()).containsEntry("backend", "unavailable");
    }

    @Test
    void health_includesLastSuccessfulBackendCheckWhenPresent() {
        Instant when = Instant.parse("2026-05-23T08:00:00Z");
        when(connectivityMonitor.snapshot()).thenReturn(
                new BackendConnectivityMonitor.ConnectivitySnapshot(true, true, when));

        Health health = indicator.health();

        assertThat(health.getDetails()).containsEntry("authentik", "ok");
        assertThat(health.getDetails()).containsEntry("backend", "ok");
        assertThat(health.getDetails()).containsEntry("lastSuccessfulBackendCheck", when.toString());
    }
}
