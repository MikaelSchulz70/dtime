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
            "Time entry grid for ONE user (admin). Requires numeric userId. "
                    + "view: WEEK or MONTH. date: any ISO day inside that week/month (e.g. 2026-05-01 for May 2026). "
                    + "Do NOT use for org-wide monthly totals — use getReport with type=USER instead.")
    public String getUserTimeReport(long userId, String view, String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("userId", userId);
        params.put("view", view);
        params.put("date", date);
        return getJson(buildPath("/api/timereport/user", params));
    }

    @Tool(description =
            "Vacation balances for the month containing date (admin). "
                    + "Omit date for current month. date: ISO YYYY-MM-DD inside target month.")
    public String getVacationReport(String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "date", date);
        return getJson(buildPath("/api/timereport/vacations", params));
    }
}
