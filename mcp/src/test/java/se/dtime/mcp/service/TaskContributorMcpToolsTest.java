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
class TaskContributorMcpToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private TaskContributorMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new TaskContributorMcpTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void getTaskContributors_usesCorrectPath() throws Exception {
        when(backendApiClient.get("/api/taskcontributor/99", Object.class)).thenReturn(Map.of());

        tools.getTaskContributors(99);

        verify(backendApiClient).get("/api/taskcontributor/99", Object.class);
    }
}
