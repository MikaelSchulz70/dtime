package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class ReportMcpTools extends McpToolsSupport {

    public ReportMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "Admin report for the month/year period that contains date. "
                    + "Omit date to use today. view: MONTH or YEAR. "
                    + "type: USER (per-user hours in period — use for 'how many users reported in May'), "
                    + "USER_TASK, TASK, ACCOUNT, BILLABLE_TASK_TYPE. "
                    + "date: any ISO day inside the target period (e.g. 2026-05-15 for May 2026). "
                    + "Response includes userReports[] with totalTime when type=USER.")
    public String getReport(String view, String type, String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "view", view);
        putIfPresent(params, "type", type);
        putIfPresent(params, "date", date);
        return getJson(buildPath("/api/report", params));
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
