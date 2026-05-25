package se.dtime.service.period;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeriodNavigationTest {

    @Test
    void shiftMonth_crossesYearBoundaryForward() {
        PeriodNavigation.MonthPeriod next = PeriodNavigation.shiftMonth(
                new PeriodNavigation.MonthPeriod(2026, 12), 1);
        assertEquals(2027, next.year());
        assertEquals(1, next.month());
    }

    @Test
    void shiftMonth_crossesYearBoundaryBackward() {
        PeriodNavigation.MonthPeriod prev = PeriodNavigation.shiftMonth(
                new PeriodNavigation.MonthPeriod(2026, 1), -1);
        assertEquals(2025, prev.year());
        assertEquals(12, prev.month());
    }

    @Test
    void shiftWeek_crossesYearBoundaryForward() {
        PeriodNavigation.WeekPeriod next = PeriodNavigation.shiftWeek(
                new PeriodNavigation.WeekPeriod(2025, 52), 1);
        assertEquals(2026, next.year());
        assertEquals(1, next.week());
    }

    @Test
    void shiftWeek_week53ToWeek1() {
        PeriodNavigation.WeekPeriod next = PeriodNavigation.shiftWeek(
                new PeriodNavigation.WeekPeriod(2020, 53), 1);
        assertEquals(2021, next.year());
        assertEquals(1, next.week());
    }

    @Test
    void shiftWeek_week1ToWeek53() {
        PeriodNavigation.WeekPeriod prev = PeriodNavigation.shiftWeek(
                new PeriodNavigation.WeekPeriod(2021, 1), -1);
        assertEquals(2020, prev.year());
        assertEquals(53, prev.week());
    }

    @Test
    void currentMonthAndWeek_fromDate() {
        LocalDate date = LocalDate.of(2026, 7, 15);
        PeriodNavigation.MonthPeriod month = PeriodNavigation.currentMonth(date);
        assertEquals(2026, month.year());
        assertEquals(7, month.month());

        PeriodNavigation.WeekPeriod week = PeriodNavigation.currentWeek(date);
        assertEquals(2026, week.year());
        assertEquals(29, week.week());
    }
}
