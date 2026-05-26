package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeReportStatusMcpToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private TimeReportStatusMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new TimeReportStatusMcpTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void getUnclosedUsers_usesPathBasedEndpoint() throws Exception {
        when(backendApiClient.get("/api/timereportstatus/year/2026/month/5", Object.class))
                .thenReturn(Map.of());

        tools.getUnclosedUsers(2026, "5", null);

        verify(backendApiClient).get("/api/timereportstatus/year/2026/month/5", Object.class);
    }

    @Test
    void getUnclosedUsers_closedTrue_addsQueryParam() throws Exception {
        when(backendApiClient.get("/api/timereportstatus/year/2026/month/5?closed=true", Object.class))
                .thenReturn(Map.of());

        tools.getUnclosedUsers(2026, "5", true);

        verify(backendApiClient).get("/api/timereportstatus/year/2026/month/5?closed=true", Object.class);
    }
}
