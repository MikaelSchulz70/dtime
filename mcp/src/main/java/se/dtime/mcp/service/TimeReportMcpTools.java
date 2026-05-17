package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class TimeReportMcpTools extends McpToolsSupport {

    public TimeReportMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "Time entry grid for ONE user only (admin). Requires numeric userId (never null). "
                    + "view: WEEK or MONTH. date: ISO YYYY-MM-DD anchor in that week/month. "
                    + "Do NOT use for 'how many users reported in May' — use getMonthlyUserReportSummary instead.")
    public String getUserTimeReport(long userId, String view, String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("userId", userId);
        params.put("view", view);
        params.put("date", date);
        return getJson(buildPath("/api/timereport/user", params));
    }

    @Tool(description = "Current vacation balances report for all users (admin)")
    public String getVacationReport() throws Exception {
        return getJson("/api/timereport/vacations");
    }

    @Tool(description =
            "Vacation balances report for the period before the given anchor date (admin). date: ISO YYYY-MM-DD.")
    public String getPreviousVacationReport(String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("date", date);
        return getJson(buildPath("/api/timereport/vacations/previous", params));
    }

    @Tool(description =
            "Vacation balances report for the period after the given anchor date (admin). date: ISO YYYY-MM-DD.")
    public String getNextVacationReport(String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("date", date);
        return getJson(buildPath("/api/timereport/vacations/next", params));
    }
}
