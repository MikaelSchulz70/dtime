package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import se.dtime.mcp.client.BackendApiClient;

import java.util.Map;

@Service
public class TaskMcpTools extends McpToolsSupport {

    public TaskMcpTools(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        super(backendApiClient, objectMapper);
    }

    @Tool(description =
            "When: full task list in one response. Optional active filter. "
                    + "Not for paginated browse (getPagedTasks) or hour reports (getReport type=TASK).")
    public String getAllTasks(
            @ToolParam(description = "Optional: true=active only, false=inactive only; omit for all", required = false)
            Boolean active) throws Exception {
        Map<String, Object> params = queryMap();
        putIfPresent(params, "active", active);
        return getJson(buildPath("/api/task", params));
    }

    @Tool(description =
            "When: list or browse projects/tasks. "
                    + "Not for aggregated hours by task (getReport with type=TASK). Returns paged tasks.")
    public String getPagedTasks(
            @ToolParam(description = "0-based page index, e.g. 0") int page,
            @ToolParam(description = "Page size, e.g. 10 or 20") int size,
            @ToolParam(description =
                    "Sort field: id, name, activationStatus, taskType, or isBillable (not taskId — use id)")
            String sort,
            @ToolParam(description = "Sort direction: asc or desc") String direction) throws Exception {
        String mappedSort = PagedApiSort.tasks(sort);
        Map<String, Object> params = queryMap();
        params.put("page", page);
        params.put("size", size);
        params.put("sort", mappedSort);
        params.put("direction", direction);
        return getJson(buildPath("/api/task/paged", params));
    }

    @Tool(description =
            "When: one task's full record by numeric id. "
                    + "Get id from getPagedTasks first. Not for lists or hour reports.")
    public String getTask(@ToolParam(description = "Numeric DTime task id") long taskId) throws Exception {
        return getJson("/api/task/" + taskId);
    }
}
