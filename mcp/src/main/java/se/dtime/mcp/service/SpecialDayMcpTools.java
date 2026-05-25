package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class SpecialDayMcpTools extends McpToolsSupport {

    public SpecialDayMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: all configured special days and holidays. "
                    + "For one year use getSpecialDaysByYear. Returns full special-day list.")
    public String getAllSpecialDays() throws Exception {
        return getJson("/api/specialday");
    }

    @Tool(description =
            "When: which calendar years have special days configured. Returns year list.")
    public String getSpecialDayYears() throws Exception {
        return getJson("/api/specialday/years");
    }

    @Tool(description =
            "When: holidays and special days for a specific calendar year. Returns days for that year.")
    public String getSpecialDaysByYear(
            @ToolParam(description = "Four-digit calendar year, e.g. 2026") int year) throws Exception {
        return getJson("/api/specialday/year/" + year);
    }

    @Tool(description =
            "When: one special day record by numeric id. Get id from getSpecialDaysByYear first.")
    public String getSpecialDay(@ToolParam(description = "Numeric special-day id") long id) throws Exception {
        return getJson("/api/specialday/" + id);
    }
}
