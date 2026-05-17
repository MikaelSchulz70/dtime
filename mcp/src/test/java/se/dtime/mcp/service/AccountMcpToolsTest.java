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
class AccountMcpToolsTest {

    @Mock
    private BackendApiClient backendApiClient;

    private AccountMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new AccountMcpTools(backendApiClient, new ObjectMapper());
    }

    @Test
    void getPagedAccounts_usesSingularAccountPath() throws Exception {
        when(backendApiClient.get(
                "/api/account/paged?page=0&size=10&sort=name&direction=asc", Object.class))
                .thenReturn(Map.of());

        tools.getPagedAccounts(0, 10, "name", "asc");

        verify(backendApiClient).get(
                "/api/account/paged?page=0&size=10&sort=name&direction=asc", Object.class);
    }

    @Test
    void getAccount_usesSingularAccountPath() throws Exception {
        when(backendApiClient.get("/api/account/7", Object.class)).thenReturn(Map.of());

        tools.getAccount(7);

        verify(backendApiClient).get("/api/account/7", Object.class);
    }

    @Test
    void getPagedAccounts_mapsAccountIdSortToId() throws Exception {
        when(backendApiClient.get(
                "/api/account/paged?page=0&size=5&sort=id&direction=desc", Object.class))
                .thenReturn(Map.of());

        tools.getPagedAccounts(0, 5, "accountId", "desc");

        verify(backendApiClient).get(
                "/api/account/paged?page=0&size=5&sort=id&direction=desc", Object.class);
    }
}
