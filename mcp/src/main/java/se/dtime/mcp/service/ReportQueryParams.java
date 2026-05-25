package se.dtime.mcp.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Normalizes {@code getReport} arguments from LLMs that often swap view/type, pass ReportType as view,
 * or put month names (e.g. APRIL) in {@code view} instead of {@code date}.
 */
final class ReportQueryParams {

    static final Set<String> VIEWS = Set.of("MONTH", "YEAR");
    static final Set<String> TYPES = Set.of("USER", "USER_TASK", "TASK", "ACCOUNT");

    private static final Map<String, Month> MONTH_ALIASES = Map.ofEntries(
            Map.entry("JAN", Month.JANUARY),
            Map.entry("FEB", Month.FEBRUARY),
            Map.entry("MAR", Month.MARCH),
            Map.entry("APR", Month.APRIL),
            Map.entry("MAY", Month.MAY),
            Map.entry("JUN", Month.JUNE),
            Map.entry("JUL", Month.JULY),
            Map.entry("AUG", Month.AUGUST),
            Map.entry("SEP", Month.SEPTEMBER),
            Map.entry("SEPT", Month.SEPTEMBER),
            Map.entry("OCT", Month.OCTOBER),
            Map.entry("NOV", Month.NOVEMBER),
            Map.entry("DEC", Month.DECEMBER));

    record Normalized(String view, String type, String date) {
    }

    private ReportQueryParams() {
    }

    static Normalized normalize(String view, String type, String date) {
        String viewToken = normalizeToken(view);
        String typeToken = normalizeToken(type);
        String dateToken = normalizeDate(date);

        MonthResolution monthInView = resolveMonthInView(viewToken, dateToken);
        viewToken = monthInView.view();
        dateToken = monthInView.date();

        if (isType(viewToken) && isView(typeToken)) {
            String swappedView = typeToken;
            String swappedType = viewToken;
            viewToken = swappedView;
            typeToken = swappedType;
        } else if (isType(viewToken) && !isView(viewToken)) {
            if (typeToken == null || (!isType(typeToken) && !isView(typeToken))) {
                typeToken = viewToken;
                viewToken = "MONTH";
            }
        } else if (isView(typeToken) && !isType(typeToken) && !isView(viewToken)) {
            viewToken = typeToken;
            typeToken = "USER";
        }

        if (viewToken == null) {
            viewToken = "MONTH";
        }
        if (typeToken == null) {
            typeToken = "USER";
        }

        if (!isView(viewToken)) {
            throw invalid("view", viewToken, VIEWS,
                    "Use MONTH or YEAR for view (period granularity). "
                            + "Put the target month in date as YYYY-MM-DD (e.g. 2026-04-15 for April 2026), "
                            + "not in view.");
        }
        if (!isType(typeToken)) {
            throw invalid("type", typeToken, TYPES,
                    "Use USER, USER_TASK, TASK, or ACCOUNT for type (breakdown). "
                            + "Use MONTH or YEAR for view (period).");
        }

        return new Normalized(viewToken, typeToken, dateToken);
    }

    private record MonthResolution(String view, String date) {
    }

    private static MonthResolution resolveMonthInView(String viewToken, String dateToken) {
        Optional<Month> month = parseMonthName(viewToken);
        if (month.isEmpty()) {
            return new MonthResolution(viewToken, dateToken);
        }
        String resolvedDate = dateToken != null ? dateToken : anchorDate(month.get(), LocalDate.now().getYear());
        return new MonthResolution("MONTH", resolvedDate);
    }

    static Optional<Month> parseMonthName(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String upper = token.trim().toUpperCase(Locale.ROOT);
        Month alias = MONTH_ALIASES.get(upper);
        if (alias != null) {
            return Optional.of(alias);
        }
        try {
            return Optional.of(Month.valueOf(upper));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    static String anchorDate(Month month, int year) {
        int day = Math.min(15, month.length(LocalDate.of(year, 1, 1).isLeapYear()));
        return LocalDate.of(year, month, day).toString();
    }

    private static String normalizeToken(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    static String normalizeDate(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return trimmed;
        }
        if (trimmed.matches("\\d{4}-\\d{2}")) {
            return trimmed + "-15";
        }
        Optional<Month> month = parseMonthName(trimmed);
        if (month.isPresent()) {
            return anchorDate(month.get(), LocalDate.now().getYear());
        }
        return trimmed;
    }

    private static boolean isView(String token) {
        return token != null && VIEWS.contains(token);
    }

    private static boolean isType(String token) {
        return token != null && TYPES.contains(token);
    }

    private static IllegalArgumentException invalid(
            String param, String value, Set<String> allowed, String hint) {
        return new IllegalArgumentException(
                "Invalid getReport " + param + " '" + value + "'. Allowed: " + allowed + ". " + hint);
    }
}
