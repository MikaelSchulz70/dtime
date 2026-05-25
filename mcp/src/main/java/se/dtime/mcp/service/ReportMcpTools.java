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
            "When: admin org-wide monthly report — all users in one call (preferred over getUserTimeReportForMonth). "
                    + "type=USER_TASK: richest (per-user task breakdown, closed flags, org totals). "
                    + "type=USER: per-user totals only. type=TASK|ACCOUNT: org aggregates by task or account. "
                    + "type=BILLABLE_TASK_TYPE: billable vs non-billable hours by task type for the month. "
                    + "For one person's totals, use type=USER and filter userReports. "
                    + "Use getUserTimeReportForMonth only for day-by-day entry grids.")
    public String getReport(
            @ToolParam(description = "Calendar year, e.g. 2026. Omit for current year.", required = false)
            Integer year,
            @ToolParam(description = "Month 1-12 or month name (e.g. 4 or APRIL). Omit for current month.", required = false)
            String month,
            @ToolParam(description =
                    "ReportType: USER_TASK, USER, TASK, ACCOUNT, or BILLABLE_TASK_TYPE. Prefer USER_TASK for analysis.")
            String type) throws Exception {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(year, month);
        String reportType = ReportPeriodParams.normalizeType(type);
        if ("BILLABLE_TASK_TYPE".equals(reportType)) {
            return getBillableTaskTypeReport(period.fromDate().toString(), period.toDate().toString());
        }
        Map<String, Object> params = queryMap();
        params.put("type", reportType);
        return getJson(buildPath("/api/report/year/" + period.year() + "/month/" + period.month(), params));
    }

    @Tool(description =
            "When: admin org-wide yearly report — same type semantics as getReport "
                    + "(USER_TASK, USER, TASK, ACCOUNT, BILLABLE_TASK_TYPE).")
    public String getYearlyReport(
            @ToolParam(description = "Calendar year, e.g. 2026. Omit for current year.", required = false)
            Integer year,
            @ToolParam(description =
                    "ReportType: USER_TASK, USER, TASK, ACCOUNT, or BILLABLE_TASK_TYPE. Prefer USER_TASK for analysis.")
            String type) throws Exception {
        ReportPeriodParams.YearPeriod period = ReportPeriodParams.resolveYearPeriod(year);
        String reportType = ReportPeriodParams.normalizeType(type);
        if ("BILLABLE_TASK_TYPE".equals(reportType)) {
            return getBillableTaskTypeReport(period.fromDate().toString(), period.toDate().toString());
        }
        Map<String, Object> params = queryMap();
        params.put("type", reportType);
        return getJson(buildPath("/api/report/year/" + period.year(), params));
    }

    @Tool(description =
            "When: billable hours grouped by task type for an arbitrary date range. "
                    + "For a calendar month or year, prefer getReport/getYearlyReport with type=BILLABLE_TASK_TYPE.")
    public String getBillableTaskTypeReport(
            @ToolParam(description = "Range start YYYY-MM-DD (inclusive)") String fromDate,
            @ToolParam(description = "Range end YYYY-MM-DD (inclusive)") String toDate) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        return getJson(buildPath("/api/report/billable-task-type", params));
    }
}
