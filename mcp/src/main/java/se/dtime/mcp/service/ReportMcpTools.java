package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class ReportMcpTools extends McpToolsSupport {

    public ReportMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: org-wide or per-user aggregated hours for a month or year "
                    + "('hours per user in May', 'report for April 2026'). "
                    + "view=MONTH|YEAR only; type=USER|USER_TASK|TASK|ACCOUNT only. "
                    + "For a specific month use view=MONTH and date=YYYY-MM-DD (e.g. 2026-04-15 for April). "
                    + "Returns period report JSON.")
    public String getReport(
            @ToolParam(description =
                    "ReportView ONLY: MONTH or YEAR. Not month names — use date for April/May (e.g. 2026-04-15).")
            String view,
            @ToolParam(description =
                    "ReportType ONLY: USER, USER_TASK, TASK, or ACCOUNT.")
            String type,
            @ToolParam(description =
                    "Target period: YYYY-MM-DD (any day in month/year), or YYYY-MM. "
                            + "Example April 2026: 2026-04-15. Omit for current period.", required = false)
            String date) throws Exception {
        ReportQueryParams.Normalized args = ReportQueryParams.normalize(view, type, date);
        Map<String, Object> params = queryMap();
        putIfPresent(params, "view", args.view());
        putIfPresent(params, "type", args.type());
        putIfPresent(params, "date", args.date());
        return getJson(buildPath("/api/report", params));
    }

    @Tool(description =
            "When: billable hours grouped by task type between two dates. "
                    + "Not for org monthly reports (getReport). Returns billable breakdown JSON.")
    public String getBillableTaskTypeReport(
            @ToolParam(description = "Range start YYYY-MM-DD (inclusive)") String fromDate,
            @ToolParam(description = "Range end YYYY-MM-DD (inclusive)") String toDate) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        return getJson(buildPath("/api/report/billable-task-type", params));
    }
}
