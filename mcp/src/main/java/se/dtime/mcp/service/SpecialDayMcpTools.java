package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class SpecialDayMcpTools extends McpToolsSupport {

    public SpecialDayMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description = "List all special days / holidays (admin)")
    public String getAllSpecialDays() throws Exception {
        return getJson("/api/specialday");
    }

    @Tool(description = "Years that have special days configured (admin)")
    public String getSpecialDayYears() throws Exception {
        return getJson("/api/specialday/years");
    }

    @Tool(description = "Special days for a calendar year (admin)")
    public String getSpecialDaysByYear(int year) throws Exception {
        return getJson("/api/specialday/year/" + year);
    }

    @Tool(description = "Get special day by id (admin)")
    public String getSpecialDay(long id) throws Exception {
        return getJson("/api/specialday/" + id);
    }
}
