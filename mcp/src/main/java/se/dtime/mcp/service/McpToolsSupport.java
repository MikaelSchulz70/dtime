package se.dtime.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import se.dtime.mcp.client.BackendApiClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared helpers for read-only MCP tools backed by {@link BackendApiClient} GET calls.
 */
@Slf4j
abstract class McpToolsSupport {

    private final BackendApiClient backendApiClient;
    private final ObjectMapper objectMapper;

    McpToolsSupport(BackendApiClient backendApiClient, ObjectMapper objectMapper) {
        this.backendApiClient = backendApiClient;
        this.objectMapper = objectMapper;
    }

    String getJson(String path) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(backendGet(path));
    }

    Object backendGet(String path) {
        String toolName = callerToolName();
        long startNanos = System.nanoTime();
        log.info("MCP tool {} -> GET {}", toolName, path);
        try {
            Object body = backendApiClient.get(path, Object.class);
            log.info("MCP tool {} completed ({}ms)", toolName, elapsedMs(startNanos));
            return body;
        } catch (RuntimeException e) {
            log.warn("MCP tool {} failed after {}ms: {}", toolName, elapsedMs(startNanos), e.getMessage());
            throw e;
        }
    }

    /** Name of the @Tool method that invoked {@link #backendGet} or {@link #getJson}. */
    static String callerToolName() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(stream -> stream
                        .skip(1)
                        .filter(frame -> !isSupportFrame(frame))
                        .findFirst()
                        .map(StackWalker.StackFrame::getMethodName)
                        .orElse("unknown"));
    }

    private static boolean isSupportFrame(StackWalker.StackFrame frame) {
        if (!McpToolsSupport.class.isAssignableFrom(frame.getDeclaringClass())) {
            return false;
        }
        return switch (frame.getMethodName()) {
            case "backendGet", "getJson", "callerToolName" -> true;
            default -> false;
        };
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    String toCompactJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    static long totalElementsFromPagedResponse(Object body) {
        if (body instanceof Map<?, ?> map) {
            Object total = map.get("totalElements");
            if (total instanceof Number number) {
                return number.longValue();
            }
        }
        throw new IllegalStateException("Paged response missing totalElements");
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
