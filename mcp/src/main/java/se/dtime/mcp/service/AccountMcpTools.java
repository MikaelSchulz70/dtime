package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Slf4j
@Service
public class AccountMcpTools extends McpToolsSupport {

    public AccountMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "List all accounts (admin). Optional active=true|false filters by activation. "
                    + "Prefer getPagedAccounts for large datasets.")
    public String getAllAccounts(Boolean active) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "active", active);
        return getJson(buildPath("/api/account", params));
    }

    @Tool(description =
            "List accounts with pagination. page is 0-based. sort: id, name, activationStatus "
                    + "(not accountId — use id). direction: asc or desc.")
    public String getPagedAccounts(int page, int size, String sort, String direction) throws Exception {
        String mappedSort = PagedApiSort.accounts(sort);
        log.debug("MCP getPagedAccounts page={} sort={}", page, mappedSort);
        Map<String, Object> params = queryMap();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", mappedSort);
        params.put("direction", direction);
        return getJson(buildPath("/api/account/paged", params));
    }

    @Tool(description = "Get account by id")
    public String getAccount(long accountId) throws Exception {
        return getJson("/api/account/" + accountId);
    }
}
