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

    @Tool(description =
            "Users with unclosed time reports for the month containing date (admin). "
                    + "Omit date for current month. date: ISO YYYY-MM-DD inside target month.")
    public String getUnclosedUsers(String date) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "date", date);
        return getJson(buildPath("/api/timereportstatus", params));
    }
}
