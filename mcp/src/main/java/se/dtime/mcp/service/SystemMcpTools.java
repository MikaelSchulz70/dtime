package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class SystemMcpTools extends McpToolsSupport {

    public SystemMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: DTime system configuration settings. Not for user, task, or time data.")
    public String getSystemConfig() throws Exception {
        return getJson("/api/system/config");
    }

    @Tool(description =
            "When: whether outbound email is enabled. Not for user or report questions.")
    public String getMailEnabled() throws Exception {
        return getJson("/api/system/mail/enabled");
    }
}
