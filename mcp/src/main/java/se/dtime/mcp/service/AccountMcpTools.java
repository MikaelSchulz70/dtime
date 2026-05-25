package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class AccountMcpTools extends McpToolsSupport {

    public AccountMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: full account list in one response. Optional active filter. "
                    + "Not for paginated browse (getPagedAccounts) or hour reports (getReport type=ACCOUNT).")
    public String getAllAccounts(
            @ToolParam(description = "Optional: true=active only, false=inactive only; omit for all", required = false)
            Boolean active) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "active", active);
        return getJson(buildPath("/api/account", params));
    }

    @Tool(description =
            "When: list or browse customer/account organizations. "
                    + "Not for aggregated hours by account (getReport with type=ACCOUNT). Returns paged accounts.")
    public String getPagedAccounts(
            @ToolParam(description = "0-based page index, e.g. 0") int page,
            @ToolParam(description = "Page size, e.g. 10 or 20") int size,
            @ToolParam(description = "Sort field: id, name, or activationStatus (not accountId — use id)")
            String sort,
            @ToolParam(description = "Sort direction: asc or desc") String direction) throws Exception {
        String mappedSort = PagedApiSort.accounts(sort);
        Map<String, Object> params = queryMap();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", mappedSort);
        params.put("direction", direction);
        return getJson(buildPath("/api/account/paged", params));
    }

    @Tool(description =
            "When: one account's full record by numeric id. "
                    + "Get id from getPagedAccounts first. Not for lists or hour reports.")
    public String getAccount(@ToolParam(description = "Numeric DTime account id") long accountId)
            throws Exception {
        return getJson("/api/account/" + accountId);
    }
}
