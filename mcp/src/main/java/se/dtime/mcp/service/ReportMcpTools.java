package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.time.LocalDate;
import java.util.Map;

@Service
public class ReportMcpTools extends McpToolsSupport {

    public ReportMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "BEST FOR: how many users reported time in a month, total hours per user, org-wide monthly totals. "
                    + "Returns admin Report JSON with userReports[] (userId, fullName, totalTime, taskReports). "
                    + "Pass dateInMonth as any ISO day inside the target month (e.g. 2026-05-15 for May 2026). "
                    + "Do NOT use getUserTimeReport for this — that tool is for one user only.")
    public String getMonthlyUserReportSummary(String dateInMonth) throws Exception {
        LocalDate inMonth = LocalDate.parse(dateInMonth);
        String apiDate = inMonth.plusMonths(1).withDayOfMonth(1).toString();
        Map<String, Object> params = queryMap();
        params.put("view", "MONTH");
        params.put("type", "USER");
        params.put("date", apiDate);
        return getJson(buildPath("/api/report/previous", params));
    }

    @Tool(description =
            "Admin report for the CURRENT period (today's month or year). "
                    + "Use for org-wide questions about this month: set view=MONTH and type=USER. "
                    + "view: MONTH or YEAR. type: USER (per-user hours), USER_TASK, TASK, ACCOUNT, BILLABLE_TASK_TYPE. "
                    + "For a past/future month use getMonthlyUserReportSummary or getPreviousReport/getNextReport.")
    public String getCurrentReports(String view, String type) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "view", view);
        putIfPresent(params, "type", type);
        return getJson(buildPath("/api/report", params));
    }

    @Tool(description =
            "Admin report for the period BEFORE the anchor date's period. "
                    + "For MONTH view, date=2026-06-01 returns May 2026 (not May when date is in May). "
                    + "Prefer getMonthlyUserReportSummary for 'users who reported in May'. "
                    + "view: MONTH or YEAR. type: USER for per-user monthly totals. date: ISO YYYY-MM-DD.")
    public String getPreviousReport(String view, String type, String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "view", view);
        putIfPresent(params, "type", type);
        params.put("date", date);
        return getJson(buildPath("/api/report/previous", params));
    }

    @Tool(description =
            "Admin report for the period AFTER the anchor date's period. "
                    + "For MONTH view, date=2026-04-15 returns May 2026. "
                    + "view: MONTH or YEAR. type: USER for per-user monthly totals. date: ISO YYYY-MM-DD.")
    public String getNextReport(String view, String type, String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "view", view);
        putIfPresent(params, "type", type);
        params.put("date", date);
        return getJson(buildPath("/api/report/next", params));
    }

    @Tool(description =
            "Billable hours by task type between two dates (admin). fromDate and toDate: ISO YYYY-MM-DD.")
    public String getBillableTaskTypeReport(String fromDate, String toDate) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        return getJson(buildPath("/api/report/billable-task-type", params));
    }
}
