package se.dtime.mcp.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class ReportPeriodParams {

    static final Set<String> TYPES = Set.of("USER", "USER_TASK", "TASK", "ACCOUNT", "BILLABLE_TASK_TYPE");

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

    record MonthPeriod(int year, int month) {
        LocalDate fromDate() {
            return LocalDate.of(year, month, 1);
        }

        LocalDate toDate() {
            LocalDate start = fromDate();
            return start.withDayOfMonth(start.lengthOfMonth());
        }
    }

    record YearPeriod(int year) {
        LocalDate fromDate() {
            return LocalDate.of(year, 1, 1);
        }

        LocalDate toDate() {
            return LocalDate.of(year, 12, 31);
        }
    }

    private ReportPeriodParams() {
    }

    static MonthPeriod resolveMonthPeriod(Integer year, Object monthInput) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        int resolvedMonth = parseMonth(monthInput, LocalDate.now().getMonthValue());
        validateYear(resolvedYear);
        validateMonth(resolvedMonth);
        return new MonthPeriod(resolvedYear, resolvedMonth);
    }

    static YearPeriod resolveYearPeriod(Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        validateYear(resolvedYear);
        return new YearPeriod(resolvedYear);
    }

    static String normalizeType(String type) {
        String token = normalizeToken(type);
        if (token == null) {
            return "USER";
        }
        if (!TYPES.contains(token)) {
            throw new IllegalArgumentException(
                    "Invalid getReport type '" + type + "'. Allowed: " + TYPES);
        }
        return token;
    }

    private static int parseMonth(Object monthInput, int defaultMonth) {
        if (monthInput == null) {
            return defaultMonth;
        }
        if (monthInput instanceof Number number) {
            return number.intValue();
        }
        String token = normalizeToken(monthInput.toString());
        if (token == null) {
            return defaultMonth;
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {
            Optional<Month> month = parseMonthName(token);
            if (month.isPresent()) {
                return month.get().getValue();
            }
            throw new IllegalArgumentException("Invalid month '" + monthInput + "'");
        }
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

    private static void validateYear(int year) {
        if (year < 1970 || year > 9999) {
            throw new IllegalArgumentException("Invalid year " + year);
        }
    }

    private static void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month " + month + ". Must be 1-12.");
        }
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
}
