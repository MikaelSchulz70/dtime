package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.UriComponentsBuilder;
import se.dtime.mcp.client.BackendApiClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared helpers for read-only MCP tools backed by {@link BackendApiClient} GET calls.
 */
abstract class McpToolsSupport {

    private final BackendApiClient backendApiClient;
    private final ObjectMapper objectMapper;

    McpToolsSupport(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        this.backendApiClient = backendApiClient;
        this.objectMapper = objectMapper;
    }

    String getJson(String path) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(backendApiClient.get(path, Object.class));
    }

    static String buildPath(String path, Map<String, ?> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (value != null) {
                    if (value instanceof String s) {
                        if (!s.isBlank()) {
                            builder.queryParam(key, s);
                        }
                    } else {
                        builder.queryParam(key, value);
                    }
                }
            });
        }
        return builder.build().toUriString();
    }

    static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String s && s.isBlank()) {
            return;
        }
        map.put(key, value);
    }

    static Map<String, Object> queryMap() {
        return new LinkedHashMap<>();
    }
}
