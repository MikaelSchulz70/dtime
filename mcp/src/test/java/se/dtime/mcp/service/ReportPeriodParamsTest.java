package se.dtime.mcp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportPeriodParamsTest {

    @Test
    void resolveMonthPeriod_acceptsNumericMonth() {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(2026, 7);
        assertEquals(2026, period.year());
        assertEquals(7, period.month());
    }

    @Test
    void resolveMonthPeriod_acceptsMonthName() {
        ReportPeriodParams.MonthPeriod period = ReportPeriodParams.resolveMonthPeriod(2026, "APRIL");
        assertEquals(2026, period.year());
        assertEquals(4, period.month());
    }

    @Test
    void resolveMonthPeriod_rejectsInvalidMonth() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportPeriodParams.resolveMonthPeriod(2026, 13));
    }

    @Test
    void normalizeType_defaultsToUser() {
        assertEquals("USER", ReportPeriodParams.normalizeType(null));
    }

    @Test
    void normalizeType_acceptsBillableTaskType() {
        assertEquals("BILLABLE_TASK_TYPE", ReportPeriodParams.normalizeType("billable_task_type"));
    }

    @Test
    void normalizeType_rejectsInvalidType() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportPeriodParams.normalizeType("INVALID"));
    }
}
