package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class TaskMcpTools extends McpToolsSupport {

    public TaskMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "List all tasks (admin). Optional active=true|false filters by activation. "
                    + "Prefer getPagedTasks for large datasets.")
    public String getAllTasks(Boolean active) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "active", active);
        return getJson(buildPath("/api/task", params));
    }

    @Tool(description =
            "List tasks with pagination. page is 0-based. sort: id, name, activationStatus, taskType, isBillable "
                    + "(not taskId — use id). direction: asc or desc.")
    public String getPagedTasks(int page, int size, String sort, String direction) throws Exception {
        String mappedSort = PagedApiSort.tasks(sort);
        Map<String, Object> params = queryMap();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", mappedSort);
        params.put("direction", direction);
        return getJson(buildPath("/api/task/paged", params));
    }

    @Tool(description = "Get task by id")
    public String getTask(long taskId) throws Exception {
        return getJson("/api/task/" + taskId);
    }
}
