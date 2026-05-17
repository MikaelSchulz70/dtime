package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

/**
 * Read-only MCP tools mapped to GET {@code /api/**} endpoints. Mutations are intentionally not exposed.
 * Failures propagate as exceptions so Spring AI MCP sets {@code isError: true} on tool results.
 */
@Slf4j
@Service
public class DtimeMcpToolsService {

    private final BackendApiClient backendApiClient;
    private final ObjectMapper objectMapper;

    public DtimeMcpToolsService(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        this.backendApiClient = backendApiClient;
        this.objectMapper = objectMapper;
    }

    private String prettyWrite(Object payload) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    }

    private String getJson(String endpoint) throws Exception {
        return prettyWrite(backendApiClient.get(endpoint, Object.class));
    }

    @Tool(description = "List users with pagination (page, size, sort field, asc or desc)")
    public String getPagedUsers(int page, int size, String sort, String direction) throws Exception {
        log.debug("MCP getPagedUsers page={} size={} sort={} direction={}", page, size, sort, direction);
        String url = String.format("/api/users/paged?page=%d&size=%d&sort=%s&direction=%s",
                page, size, sort, direction);
        return getJson(url);
    }

    @Tool(description = "Get user by numeric id")
    public String getUser(long userId) throws Exception {
        log.debug("MCP getUser userId={}", userId);
        return getJson("/api/users/" + userId);
    }

    @Tool(description = "List accounts with pagination")
    public String getPagedAccounts(int page, int size, String sort, String direction) throws Exception {
        log.debug("MCP getPagedAccounts page={}", page);
        String url = String.format("/api/accounts/paged?page=%d&size=%d&sort=%s&direction=%s",
                page, size, sort, direction);
        return getJson(url);
    }

    @Tool(description = "Get account by id")
    public String getAccount(long accountId) throws Exception {
        return getJson("/api/accounts/" + accountId);
    }

    @Tool(description = "List tasks with pagination")
    public String getPagedTasks(int page, int size, String sort, String direction) throws Exception {
        String url = String.format("/api/tasks/paged?page=%d&size=%d&sort=%s&direction=%s",
                page, size, sort, direction);
        return getJson(url);
    }

    @Tool(description = "Get task by id")
    public String getTask(long taskId) throws Exception {
        return getJson("/api/tasks/" + taskId);
    }

    @Tool(description = "Task contributors assigned to the given user id")
    public String getTaskContributors(long userId) throws Exception {
        return getJson("/api/taskcontributors/" + userId);
    }

    @Tool(description =
            "Time report for a user on a date; view must match backend TimeReportView enum name "
                    + "(e.g. WEEK, MONTH)")
    public String getUserTimeReport(long userId, String view, String date) throws Exception {
        String url = String.format("/api/timereport/user?userId=%d&view=%s&date=%s", userId, view, date);
        return getJson(url);
    }

    @Tool(description = "Current vacation balances report (admin)")
    public String getVacationReport() throws Exception {
        return getJson("/api/timereport/vacations");
    }

    @Tool(description = "Public system configuration (admin)")
    public String getSystemConfig() throws Exception {
        return getJson("/api/system/config");
    }
}
