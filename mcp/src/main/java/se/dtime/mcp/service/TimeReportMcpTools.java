package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class TimeReportMcpTools extends McpToolsSupport {

    public TimeReportMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: one user's day-by-day time-entry grid only ('which days did Alice log time in May'). "
                    + "Not for org-wide analysis, rankings, or monthly totals — use getReport "
                    + "(type=USER or USER_TASK) instead; it returns all users in one admin call. "
                    + "Requires userId from getPagedUsers.")
    public String getUserTimeReportForMonth(
            @ToolParam(description = "Numeric DTime user id (required)") long userId,
            @ToolParam(description = "Calendar year, e.g. 2026. Omit for current year.", required = false)
            Integer year,
            @ToolParam(description = "Month 1-12 or month name. Omit for current month.", required = false)
            String month) throws Exception {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(year, month);
        return getJson("/api/timereport/user/" + userId + "/year/" + period.year() + "/month/" + period.month());
    }

    @Tool(description =
            "When: one user's day-by-day time-entry grid for an ISO week only. "
                    + "Not for org-wide or monthly totals — use getReport instead. "
                    + "Requires userId from getPagedUsers.")
    public String getUserTimeReportForWeek(
            @ToolParam(description = "Numeric DTime user id (required)") long userId,
            @ToolParam(description = "ISO week-based year, e.g. 2026. Omit for current ISO year.", required = false)
            Integer year,
            @ToolParam(description = "ISO week number 1-53. Omit for current ISO week.", required = false)
            Integer week) throws Exception {
        int resolvedYear = year != null ? year : java.time.LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekBasedYear());
        int resolvedWeek = week != null ? week : java.time.LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        return getJson("/api/timereport/user/" + userId + "/year/" + resolvedYear + "/week/" + resolvedWeek);
    }

    @Tool(description =
            "When: vacation day balances for a month ('remaining vacation', 'vacation report'). "
                    + "Not for worked hours (getReport). Returns vacation balance JSON.")
    public String getVacationReport(
            @ToolParam(description = "Calendar year, e.g. 2026. Omit for current year.", required = false)
            Integer year,
            @ToolParam(description = "Month 1-12 or month name. Omit for current month.", required = false)
            String month) throws Exception {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(year, month);
        return getJson("/api/timereport/vacations/year/" + period.year() + "/month/" + period.month());
    }
}
