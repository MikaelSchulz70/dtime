package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class TimeReportStatusMcpTools extends McpToolsSupport {

    public TimeReportStatusMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: users who have not closed/submitted time reports for a month "
                    + "('unclosed reports', 'missing timesheets', 'who hasn't reported'). "
                    + "Returns list of users with open reporting status.")
    public String getUnclosedUsers(
            @ToolParam(description =
                    "Optional YYYY-MM-DD on any day in target month; omit for current month", required = false)
            String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "date", date);
        return getJson(buildPath("/api/timereportstatus", params));
    }
}
