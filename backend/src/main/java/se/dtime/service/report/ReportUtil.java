package se.dtime.service.report;

import se.dtime.model.ReportDates;
import se.dtime.model.report.ReportView;

import java.time.LocalDate;

public class ReportUtil {

    static LocalDate getNextDate(ReportView reportView, LocalDate date) {
        return switch (reportView) {
            case MONTH -> date.plusMonths(1);
            case YEAR -> date.plusYears(1);
        };
    }

    static LocalDate getPreviousDate(ReportView reportView, LocalDate date) {
        return switch (reportView) {
            case MONTH -> date.minusMonths(1);
            case YEAR -> date.minusYears(1);
        };
    }

    static ReportDates getReportDates(ReportView reportView, LocalDate date) {
        LocalDate fromDate;
        LocalDate toDate = switch (reportView) {
            case MONTH -> {
                fromDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
                yield date.withDayOfMonth(date.lengthOfMonth());
            }
            case YEAR -> {
                fromDate = LocalDate.of(date.getYear(), 1, 1);
                yield LocalDate.of(date.getYear(), 12, 31);
            }
        };

        return new ReportDates(fromDate, toDate);
    }
}
