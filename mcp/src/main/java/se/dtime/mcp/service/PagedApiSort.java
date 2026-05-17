package se.dtime.mcp.service;

/**
 * Normalizes sort field names from LLM/tool callers to values accepted by dtime-backend JPA entities.
 */
final class PagedApiSort {

    private PagedApiSort() {
    }

    static String users(String sort) {
        if (sort == null || sort.isBlank()) {
            return "displayName";
        }
        String value = sort.trim();
        return switch (value) {
            case "userId" -> "id";
            case "id", "displayName", "email", "userRole", "activationStatus", "firstName", "lastName", "externalId" ->
                    value;
            default -> "displayName";
        };
    }

    static String accounts(String sort) {
        if (sort == null || sort.isBlank()) {
            return "name";
        }
        String value = sort.trim();
        return switch (value) {
            case "accountId" -> "id";
            case "id", "name", "activationStatus" -> value;
            default -> "name";
        };
    }

    static String tasks(String sort) {
        if (sort == null || sort.isBlank()) {
            return "name";
        }
        String value = sort.trim();
        return switch (value) {
            case "taskId" -> "id";
            case "id", "name", "activationStatus", "taskType", "isBillable" -> value;
            default -> "name";
        };
    }
}
