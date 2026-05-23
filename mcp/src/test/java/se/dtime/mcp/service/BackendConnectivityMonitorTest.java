package se.dtime.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.mcp.client.BackendApiClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackendConnectivityMonitorTest {

    @Mock
    private BackendApiClient backendApiClient;

    @InjectMocks
    private BackendConnectivityMonitor monitor;

    @BeforeEach
    void resetMocks() {
        when(backendApiClient.canObtainAccessToken()).thenReturn(false);
    }

    @Test
    void checkNow_marksBothUnavailableWhenAuthentikFails() {
        BackendConnectivityMonitor.ConnectivitySnapshot snapshot = monitor.checkNow();

        assertThat(snapshot.authentikReachable()).isFalse();
        assertThat(snapshot.backendReachable()).isFalse();
        assertThat(snapshot.authentikStatus()).isEqualTo("unavailable");
        verify(backendApiClient, times(1)).canObtainAccessToken();
    }

    @Test
    void checkNow_marksBackendUnavailableWhenTokenOkButApiFails() {
        when(backendApiClient.canObtainAccessToken()).thenReturn(true);
        when(backendApiClient.canAccessBackendApi()).thenReturn(false);

        BackendConnectivityMonitor.ConnectivitySnapshot snapshot = monitor.checkNow();

        assertThat(snapshot.authentikReachable()).isTrue();
        assertThat(snapshot.backendReachable()).isFalse();
    }

    @Test
    void checkNow_marksFullyConnectedAndRecordsTimestamp() {
        when(backendApiClient.canObtainAccessToken()).thenReturn(true);
        when(backendApiClient.canAccessBackendApi()).thenReturn(true);

        BackendConnectivityMonitor.ConnectivitySnapshot snapshot = monitor.checkNow();

        assertThat(snapshot.authentikReachable()).isTrue();
        assertThat(snapshot.backendReachable()).isTrue();
        assertThat(snapshot.lastSuccessfulBackendCheck()).isNotNull();
        assertThat(monitor.isFullyConnected()).isTrue();
    }

    @Test
    void checkNow_skipsBackendProbeWhenAuthentikFails() {
        monitor.checkNow();

        verify(backendApiClient, times(0)).canAccessBackendApi();
    }
}
