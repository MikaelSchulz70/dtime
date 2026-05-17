package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Slf4j
@Service
public class UserMcpTools extends McpToolsSupport {

    public UserMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "List all users (admin). Optional active=true|false filters by activation. "
                    + "Prefer getPagedUsers for large datasets — this may return a large JSON payload.")
    public String getAllUsers(Boolean active) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "active", active);
        return getJson(buildPath("/api/users", params));
    }

    @Tool(description =
            "List users with pagination. page is 0-based. sort must be one of: id, displayName, email, "
                    + "userRole, activationStatus, firstName, lastName (maps to displayName), externalId. "
                    + "Do not use userId — use id. direction: asc or desc.")
    public String getPagedUsers(int page, int size, String sort, String direction) throws Exception {
        String mappedSort = PagedApiSort.users(sort);
        log.debug("MCP getPagedUsers page={} size={} sort={} direction={}", page, size, mappedSort, direction);
        Map<String, Object> params = queryMap();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", mappedSort);
        params.put("direction", direction);
        return getJson(buildPath("/api/users/paged", params));
    }

    @Tool(description = "Get user by numeric id")
    public String getUser(long userId) throws Exception {
        log.debug("MCP getUser userId={}", userId);
        return getJson("/api/users/" + userId);
    }
}
