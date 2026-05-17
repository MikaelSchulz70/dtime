package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class TimeReportStatusMcpTools extends McpToolsSupport {

    public TimeReportStatusMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description = "Users with unclosed time reports for the current period (admin)")
    public String getCurrentUnclosedUsers() throws Exception {
        return getJson("/api/timereportstatus");
    }

    @Tool(description =
            "Users with unclosed time reports for the period before the anchor date (admin). date: ISO YYYY-MM-DD.")
    public String getPreviousUnclosedUsers(String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("date", date);
        return getJson(buildPath("/api/timereportstatus/previous", params));
    }

    @Tool(description =
            "Users with unclosed time reports for the period after the anchor date (admin). date: ISO YYYY-MM-DD.")
    public String getNextUnclosedUsers(String date) throws Exception {
        Map<String, Object> params = queryMap();
        params.put("date", date);
        return getJson(buildPath("/api/timereportstatus/next", params));
    }
}
