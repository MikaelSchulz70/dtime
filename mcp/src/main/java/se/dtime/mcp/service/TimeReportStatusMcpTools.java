package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class TimeReportStatusMcpTools extends McpToolsSupport {

    public TimeReportStatusMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: who has or has not closed/submitted their monthly time report. "
                    + "Use for 'have all users closed their reports?', 'who hasn't submitted?', "
                    + "'missing time reports', 'unclosed reports'. "
                    + "Default (closed omitted/false): users with open/unclosed reports — "
                    + "empty unclosedUsers means everyone has closed. "
                    + "closed=true: users who have closed (for reopen/admin review). "
                    + "Not for headcount (getUserCount) or hours (getReport). "
                    + "Do not page through getPagedUsers to infer close status.")
    public String getUnclosedUsers(
            @ToolParam(description = "Calendar year, e.g. 2026. Omit for current year.", required = false)
            Integer year,
            @ToolParam(description = "Month 1-12 or month name. Omit for current month.", required = false)
            String month,
            @ToolParam(description =
                    "false/omit: users who have NOT closed (default). true: users who HAVE closed.", required = false)
            Boolean closed) throws Exception {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(year, month);
        String path = "/api/timereportstatus/year/" + period.year() + "/month/" + period.month();
        if (Boolean.TRUE.equals(closed)) {
            path += "?closed=true";
        }
        return getJson(path);
    }
}
