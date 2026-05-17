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
class TaskMcpToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private TaskMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new TaskMcpTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void getPagedTasks_usesSingularTaskPath() throws Exception {
        when(backendApiClient.get(
                "/api/task/paged?page=1&size=20&sort=name&direction=asc", Object.class))
                .thenReturn(Map.of());

        tools.getPagedTasks(1, 20, "name", "asc");

        verify(backendApiClient).get(
                "/api/task/paged?page=1&size=20&sort=name&direction=asc", Object.class);
    }

    @Test
    void getTask_usesSingularTaskPath() throws Exception {
        when(backendApiClient.get("/api/task/3", Object.class)).thenReturn(Map.of());

        tools.getTask(3);

        verify(backendApiClient).get("/api/task/3", Object.class);
    }
}
