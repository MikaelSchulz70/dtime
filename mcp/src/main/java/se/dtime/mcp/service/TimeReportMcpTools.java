package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class TimeReportMcpTools extends McpToolsSupport {

    public TimeReportMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: one user's time-entry grid for a week or month ('Alice's hours in May'). "
                    + "Requires userId from getPagedUsers. "
                    + "Not for org-wide totals (getReport with type=USER). Returns user time grid JSON.")
    public String getUserTimeReport(
            @ToolParam(description = "Numeric DTime user id (required)") long userId,
            @ToolParam(description = "Grid period: WEEK or MONTH") String view,
            @ToolParam(description =
                    "Optional YYYY-MM-DD on any day in target week/month; omit for current period",
                    required = false)
            String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("userId", userId);
        params.put("view", view);
        params.put("date", date);
        return getJson(buildPath("/api/timereport/user", params));
    }

    @Tool(description =
            "When: vacation day balances for a month ('remaining vacation', 'vacation report'). "
                    + "Not for worked hours (getReport). Returns vacation balance JSON.")
    public String getVacationReport(
            @ToolParam(description =
                    "Optional YYYY-MM-DD on any day in target month; omit for current month", required = false)
            String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "date", date);
        return getJson(buildPath("/api/timereport/vacations", params));
    }
}
