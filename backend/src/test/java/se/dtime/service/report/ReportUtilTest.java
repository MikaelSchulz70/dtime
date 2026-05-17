package se.dtime.service.report;

import org.junit.jupiter.api.Test;
import se.dtime.model.ReportDates;
import se.dtime.model.report.ReportView;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReportUtilTest {

    @Test
    public void getToFromDate() {
        LocalDate date = LocalDate.of(2018, 1, 7);

        ReportDates reportDates = ReportUtil.getReportDates(ReportView.MONTH, date);
        assertEquals(LocalDate.of(2018, 1, 1), reportDates.getFromDate());
        assertEquals(LocalDate.of(2018, 1, 31), reportDates.getToDate());

        reportDates = ReportUtil.getReportDates(ReportView.YEAR, date);
        assertEquals(LocalDate.of(2018, 1, 1), reportDates.getFromDate());
        assertEquals(LocalDate.of(2018, 12, 31), reportDates.getToDate());
    }

}