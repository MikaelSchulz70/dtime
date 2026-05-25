package se.dtime.mcp.service;

/**
 * Parses tool arguments from LLMs that often send numbers as strings (e.g. Ollama llama3.2).
 */
final class McpToolParams {

    private McpToolParams() {
    }

    static int parseInt(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(paramName + " is required");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(paramName + " must be an integer", e);
        }
    }

    static long parseLong(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(paramName + " is required");
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(paramName + " must be a number", e);
        }
    }
}
