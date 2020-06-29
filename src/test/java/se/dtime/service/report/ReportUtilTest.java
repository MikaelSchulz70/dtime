package se.dtime.service.report;

import org.junit.Test;
import se.dtime.model.ReportDates;
import se.dtime.model.report.ReportView;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class ReportUtilTest {

    @Test
    public void getNextDateTest() {
        LocalDate date = LocalDate.of(2018, 12, 15);
        assertEquals(LocalDate.of(2019, 1, 15), ReportUtil.getNextDate(ReportView.MONTH, date));
        assertEquals(LocalDate.of(2019, 12, 15), ReportUtil.getNextDate(ReportView.YEAR, date));
    }

    @Test
    public void getPreviousDateTest() {
        LocalDate date = LocalDate.of(2018, 12, 15);
        assertEquals(LocalDate.of(2018, 11, 15), ReportUtil.getPreviousDate(ReportView.MONTH, date));
        assertEquals(LocalDate.of(2017, 12, 15), ReportUtil.getPreviousDate(ReportView.YEAR, date));
    }

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