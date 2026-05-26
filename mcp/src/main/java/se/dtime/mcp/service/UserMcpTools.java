package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class UserMcpTools extends McpToolsSupport {

    public UserMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: full user list in one response. Optional active filter. "
                    + "Not for counts (getUserCount) or large lists (getPagedUsers).")
    public String getAllUsers(
            @ToolParam(description = "Optional: true=active only, false=inactive only; omit for all", required = false)
            Boolean active) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "active", active);
        return getJson(buildPath("/api/users", params));
    }

    @Tool(description =
            "When: 'how many users', total headcount. "
                    + "Not for time report close/submit status (getUnclosedUsers), names, or lists (getPagedUsers). "
                    + "Returns JSON {totalElements: N} only.")
    public String getUserCount() throws Exception {
        long total = totalElementsFromPagedResponse(
                backendGet("/api/users/paged?page=0&size=1&sort=id&direction=asc"));
        return toCompactJson(Map.of("totalElements", total));
    }

    @Tool(description =
            "When: list or browse users (names, emails, roles). "
                    + "Not for total count (getUserCount) or time report close status (getUnclosedUsers). "
                    + "Returns paged user records.")
    public String getPagedUsers(
            @ToolParam(description = "0-based page index as string, e.g. \"0\"") String page,
            @ToolParam(description = "Page size as string, e.g. \"10\" or \"20\"") String size,
            @ToolParam(description = "Sort field: id, displayName, email, userRole, or activationStatus")
            String sort,
            @ToolParam(description = "Sort direction: asc or desc") String direction) throws Exception {
        int pageNum = McpToolParams.parseInt(page, "page");
        int sizeNum = McpToolParams.parseInt(size, "size");
        String mappedSort = PagedApiSort.users(sort);
        Map<String, Object> params = queryMap();
        params.put("page", pageNum);
        params.put("size", sizeNum);
        params.put("sort", mappedSort);
        params.put("direction", direction);
        return getJson(buildPath("/api/users/paged", params));
    }

    @Tool(description =
            "When: one user's full record by numeric id. "
                    + "Get id from getPagedUsers first. Not for counts or lists.")
    public String getUser(
            @ToolParam(description = "Numeric DTime user id (not email or name)") String userId)
            throws Exception {
        long id = McpToolParams.parseLong(userId, "userId");
        return getJson("/api/users/" + id);
    }
}
