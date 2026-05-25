package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

@Service
public class TaskContributorMcpTools extends McpToolsSupport {

    public TaskContributorMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: which tasks a user is assigned to as contributor. "
                    + "Get userId from getPagedUsers. Returns task assignments for that user.")
    public String getTaskContributors(
            @ToolParam(description = "Numeric DTime user id") long userId) throws Exception {
        return getJson("/api/taskcontributor/" + userId);
    }

    @Tool(description =
            "When: all current task-contributor assignments org-wide. "
                    + "Not for one user (getTaskContributors). Returns full assignment list.")
    public String getCurrentTaskContributors() throws Exception {
        return getJson("/api/taskcontributor/currentTaskContributors");
    }
}
