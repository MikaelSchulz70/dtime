package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class TaskContributorMcpTools extends McpToolsSupport {

    public TaskContributorMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description = "Task contributors assigned to the given user id (admin)")
    public String getTaskContributors(long userId) throws Exception {
        return getJson("/api/taskcontributor/" + userId);
    }

    @Tool(description = "All current task contributor assignments (admin)")
    public String getCurrentTaskContributors() throws Exception {
        return getJson("/api/taskcontributor/currentTaskContributors");
    }
}
