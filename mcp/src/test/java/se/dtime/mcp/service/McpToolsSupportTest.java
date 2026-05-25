package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpToolsSupportTest {

    @Mock
    private BackendApiClient backendApiClient;

    private TestTools tools;

    @BeforeEach
    void setUp() {
        tools = new TestTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void callerToolName_returnsImmediateCaller() {
        assertThat(tools.exposeCallerToolName()).isEqualTo("exposeCallerToolName");
    }

    @Test
    void callerToolName_skipsBackendGetFrame() {
        when(backendApiClient.get("/api/x", Object.class)).thenReturn(Map.of());

        assertThat(tools.simulateToolCallingBackendGet()).isEqualTo("simulateToolCallingBackendGet");
        verify(backendApiClient).get("/api/x", Object.class);
    }

    @Test
    void callerToolName_skipsGetJsonAndBackendGetFrames() throws Exception {
        when(backendApiClient.get("/api/account/1", Object.class)).thenReturn(Map.of("id", 1));

        assertThat(tools.simulateToolCallingGetJson("/api/account/1")).isEqualTo("simulateToolCallingGetJson");
        verify(backendApiClient).get("/api/account/1", Object.class);
    }

    /** Minimal concrete subclass for package-private {@link McpToolsSupport} helpers. */
    static final class TestTools extends McpToolsSupport {

        String capturedToolName;

        TestTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
            super(backendApiClient, objectMapper);
        }

        String exposeCallerToolName() {
            return callerToolName();
        }

        @Override
        Object backendGet(String path) {
            capturedToolName = callerToolName();
            return super.backendGet(path);
        }

        String simulateToolCallingBackendGet() {
            backendGet("/api/x");
            return capturedToolName;
        }

        String simulateToolCallingGetJson(String path) throws Exception {
            getJson(path);
            return capturedToolName;
        }
    }
}
