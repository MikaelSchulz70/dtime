package se.dtime.service.report;

import se.dtime.model.ReportDates;
import se.dtime.model.report.ReportView;

import java.time.LocalDate;

public class ReportUtil {

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
