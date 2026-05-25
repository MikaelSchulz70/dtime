package se.dtime.service.period;

import org.springframework.stereotype.Component;
import se.dtime.model.ReportDates;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@Component
public class PeriodResolver {

    private final PeriodValidator periodValidator;

    public PeriodResolver(PeriodValidator periodValidator) {
        this.periodValidator = periodValidator;
    }

    public ReportDates resolveMonth(int year, int month) {
        periodValidator.validateYear(year);
        periodValidator.validateMonth(month);
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        return new ReportDates(fromDate, toDate);
    }

    public ReportDates resolveYear(int year) {
        periodValidator.validateYear(year);
        return new ReportDates(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    public ReportDates resolveIsoWeek(int weekBasedYear, int week) {
        periodValidator.validateWeekOfYear(weekBasedYear, week);
        LocalDate monday = LocalDate.now()
                .with(WeekFields.ISO.weekBasedYear(), weekBasedYear)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                .with(WeekFields.ISO.dayOfWeek(), 1);
        return new ReportDates(monday, monday.plusDays(6));
    }
}
