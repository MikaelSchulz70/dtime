package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.mcp.client.BackendApiClient;
import se.dtime.mcp.model.DtimeApiException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMcpToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private UserMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new UserMcpTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void getUser_returnsPrettyJson() throws Exception {
        when(backendApiClient.get("/api/users/42", Object.class))
                .thenReturn(Map.of("id", 42, "email", "user@example.com"));

        String result = tools.getUser(42);

        assertThat(result).contains("\"id\" : 42");
        assertThat(result).contains("user@example.com");
        verify(backendApiClient).get("/api/users/42", Object.class);
    }

    @Test
    void getUser_propagatesApiFailure() {
        when(backendApiClient.get("/api/users/42", Object.class))
                .thenThrow(new DtimeApiException("API request failed: forbidden"));

        assertThatThrownBy(() -> tools.getUser(42))
                .isInstanceOf(DtimeApiException.class)
                .hasMessageContaining("forbidden");
    }

    @Test
    void getPagedUsers_buildsQueryString() throws Exception {
        when(backendApiClient.get(
                "/api/users/paged?page=0&size=10&sort=firstName&direction=asc", Object.class))
                .thenReturn(Map.of("content", java.util.List.of()));

        tools.getPagedUsers(0, 10, "firstName", "asc");

        verify(backendApiClient).get(
                "/api/users/paged?page=0&size=10&sort=firstName&direction=asc", Object.class);
    }

    @Test
    void getPagedUsers_mapsUserIdSortToId() throws Exception {
        when(backendApiClient.get(
                "/api/users/paged?page=0&size=100&sort=id&direction=asc", Object.class))
                .thenReturn(Map.of("content", java.util.List.of(), "totalElements", 5));

        tools.getPagedUsers(0, 100, "userId", "asc");

        verify(backendApiClient).get(
                "/api/users/paged?page=0&size=100&sort=id&direction=asc", Object.class);
    }

    @Test
    void getAllUsers_omitsActiveWhenNull() throws Exception {
        when(backendApiClient.get("/api/users", Object.class)).thenReturn(Map.of());

        tools.getAllUsers(null);

        verify(backendApiClient).get("/api/users", Object.class);
    }

    @Test
    void getAllUsers_includesActiveFilter() throws Exception {
        when(backendApiClient.get("/api/users?active=true", Object.class)).thenReturn(Map.of());

        tools.getAllUsers(true);

        verify(backendApiClient).get("/api/users?active=true", Object.class);
    }
}
