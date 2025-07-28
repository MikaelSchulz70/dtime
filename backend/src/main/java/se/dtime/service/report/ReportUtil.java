package se.dtime.service.report;

import se.dtime.model.ReportDates;
import se.dtime.model.report.ReportView;

import java.time.LocalDate;

public class ReportUtil {

    static LocalDate getNextDate(ReportView reportView, LocalDate date) {
        LocalDate reportDate = date;
        switch (reportView) {
            case MONTH:
                reportDate = date.plusMonths(1);
                break;
            case YEAR:
                reportDate = date.plusYears(1);
                break;
        }
        return reportDate;
    }

    static LocalDate getPreviousDate(ReportView reportView, LocalDate date) {
        LocalDate reportDate = date;
        switch (reportView) {
            case MONTH:
                reportDate = date.minusMonths(1);
                break;
            case YEAR:
                reportDate = date.minusYears(1);
                break;
        }
        return reportDate;
    }

    static ReportDates getReportDates(ReportView reportView, LocalDate date) {
        LocalDate fromDate = date;
        LocalDate toDate = date;
        switch (reportView) {
            case MONTH:
                fromDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
                toDate = date.withDayOfMonth(date.lengthOfMonth());
                break;
            case YEAR:
                fromDate = LocalDate.of(date.getYear(), 1, 1);
                toDate = LocalDate.of(date.getYear(), 12, 31);
                break;
        }

        return new ReportDates(fromDate, toDate);
    }
}
