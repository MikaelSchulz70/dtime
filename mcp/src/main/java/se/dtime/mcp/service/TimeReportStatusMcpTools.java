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
            "When: time report status for a month. By default returns users with unclosed/open reports; "
                    + "set closed=true for users who have closed their report ('reopen report', 'who has submitted').")
    public String getUnclosedUsers(
            @ToolParam(description = "Calendar year, e.g. 2026. Omit for current year.", required = false)
            Integer year,
            @ToolParam(description = "Month 1-12 or month name. Omit for current month.", required = false)
            String month,
            @ToolParam(description = "If true, return closed reports only. Default false (unclosed only).", required = false)
            Boolean closed) throws Exception {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(year, month);
        String path = "/api/timereportstatus/year/" + period.year() + "/month/" + period.month();
        if (Boolean.TRUE.equals(closed)) {
            path += "?closed=true";
        }
        return getJson(path);
    }
}
