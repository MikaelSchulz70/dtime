package se.dtime.mcp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import se.dtime.mcp.config.BackendApiConfig;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpOAuthWarmupRunnerTest {

    @Mock
    private BackendApiConfig backendApiConfig;

    @Mock
    private BackendConnectivityMonitor connectivityMonitor;

    @InjectMocks
    private McpOAuthWarmupRunner runner;

    @Test
    void run_throwsWhenOAuthConfigMissingAndFailOnMissingEnabled() {
        when(backendApiConfig.isFailOnMissingOauthConfig()).thenReturn(true);
        when(backendApiConfig.isOAuthConfigPresent()).thenReturn(false);

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments(new String[] {})))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OAuth configuration incomplete");
    }

    @Test
    void run_doesNotThrowWhenBackendUnavailableInResilientMode() {
        when(backendApiConfig.isFailOnMissingOauthConfig()).thenReturn(true);
        when(backendApiConfig.isOAuthConfigPresent()).thenReturn(true);
        when(backendApiConfig.isFailOnUnavailable()).thenReturn(false);
        when(backendApiConfig.getConnectivityCheckIntervalMs()).thenReturn(30_000L);
        when(connectivityMonitor.checkNow()).thenReturn(
                new BackendConnectivityMonitor.ConnectivitySnapshot(true, false, null));
        when(connectivityMonitor.isFullyConnected()).thenReturn(false);

        assertThatCode(() -> runner.run(new DefaultApplicationArguments(new String[] {})))
                .doesNotThrowAnyException();
    }

    @Test
    void run_throwsWhenBackendUnavailableInStrictMode() {
        when(backendApiConfig.isFailOnMissingOauthConfig()).thenReturn(true);
        when(backendApiConfig.isOAuthConfigPresent()).thenReturn(true);
        when(backendApiConfig.isFailOnUnavailable()).thenReturn(true);
        when(connectivityMonitor.checkNow()).thenReturn(
                new BackendConnectivityMonitor.ConnectivitySnapshot(true, false, null));
        when(connectivityMonitor.isFullyConnected()).thenReturn(false);

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments(new String[] {})))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("degraded mode");
    }
}
