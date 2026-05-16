package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

/**
 * Read-only MCP tools mapped to GET {@code /api/**} endpoints. Mutations are intentionally not exposed.
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

    @Tool(description = "List users with pagination (page, size, sort field, asc or desc)")
    public String getPagedUsers(int page, int size, String sort, String direction) {
        log.debug("MCP getPagedUsers page={} size={} sort={} direction={}", page, size, sort, direction);
        try {
            String url = String.format("/api/users/paged?page=%d&size=%d&sort=%s&direction=%s",
                    page, size, sort, direction);
            return prettyWrite(backendApiClient.get(url, Object.class));
        } catch (Exception e) {
            log.error("getPagedUsers failed", e);
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get user by numeric id")
    public String getUser(long userId) {
        log.debug("MCP getUser userId={}", userId);
        try {
            return prettyWrite(backendApiClient.get("/api/users/" + userId, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "List accounts with pagination")
    public String getPagedAccounts(int page, int size, String sort, String direction) {
        log.debug("MCP getPagedAccounts page={}", page);
        try {
            String url = String.format("/api/accounts/paged?page=%d&size=%d&sort=%s&direction=%s",
                    page, size, sort, direction);
            return prettyWrite(backendApiClient.get(url, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get account by id")
    public String getAccount(long accountId) {
        try {
            return prettyWrite(backendApiClient.get("/api/accounts/" + accountId, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "List tasks with pagination")
    public String getPagedTasks(int page, int size, String sort, String direction) {
        try {
            String url = String.format("/api/tasks/paged?page=%d&size=%d&sort=%s&direction=%s",
                    page, size, sort, direction);
            return prettyWrite(backendApiClient.get(url, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get task by id")
    public String getTask(long taskId) {
        try {
            return prettyWrite(backendApiClient.get("/api/tasks/" + taskId, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Task contributors assigned to the given user id")
    public String getTaskContributors(long userId) {
        try {
            return prettyWrite(backendApiClient.get("/api/taskcontributors/" + userId, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description =
            "Time report for a user on a date; view must match backend TimeReportView enum name "
                    + "(e.g. WEEK, MONTH)")
    public String getUserTimeReport(long userId, String view, String date) {
        try {
            String url = String.format("/api/timereport/user?userId=%d&view=%s&date=%s", userId, view, date);
            return prettyWrite(backendApiClient.get(url, Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Current vacation balances report (admin)")
    public String getVacationReport() {
        try {
            return prettyWrite(backendApiClient.get("/api/timereport/vacations", Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Public system configuration (admin)")
    public String getSystemConfig() {
        try {
            return prettyWrite(backendApiClient.get("/api/system/config", Object.class));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
