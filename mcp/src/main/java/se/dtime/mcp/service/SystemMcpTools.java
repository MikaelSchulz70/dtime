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

    @Tool(description = "Public system configuration (admin)")
    public String getSystemConfig() throws Exception {
        return getJson("/api/system/config");
    }

    @Tool(description = "Whether outbound mail is enabled (admin)")
    public String getMailEnabled() throws Exception {
        return getJson("/api/system/mail/enabled");
    }
}
