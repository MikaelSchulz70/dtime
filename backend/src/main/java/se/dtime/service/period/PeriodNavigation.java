package se.dtime.service.period;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public final class PeriodNavigation {

    private PeriodNavigation() {
    }

    public record MonthPeriod(int year, int month) {
    }

    public record WeekPeriod(int year, int week) {
    }

    public static MonthPeriod currentMonth(LocalDate today) {
        return new MonthPeriod(today.getYear(), today.getMonthValue());
    }

    public static WeekPeriod currentWeek(LocalDate today) {
        return new WeekPeriod(
                today.get(WeekFields.ISO.weekBasedYear()),
                today.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    public static MonthPeriod shiftMonth(MonthPeriod period, int delta) {
        LocalDate date = LocalDate.of(period.year(), period.month(), 15).plusMonths(delta);
        return new MonthPeriod(date.getYear(), date.getMonthValue());
    }

    public static WeekPeriod shiftWeek(WeekPeriod period, int delta) {
        LocalDate monday = LocalDate.now()
                .with(WeekFields.ISO.weekBasedYear(), period.year())
                .with(WeekFields.ISO.weekOfWeekBasedYear(), period.week())
                .with(WeekFields.ISO.dayOfWeek(), 1)
                .plusWeeks(delta);
        return new WeekPeriod(
                monday.get(WeekFields.ISO.weekBasedYear()),
                monday.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }
}
