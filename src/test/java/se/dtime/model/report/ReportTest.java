package se.dtime.model.report;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class ReportTest {

    @Test
    public void testCalculations() {
        Report report = new Report(LocalDate.now(), LocalDate.now(), 200);
        report.setTotalWorkableHours(200);
        report.setTotalWorkedHoursExternalProvision(80);
        report.setTotalWorkedHoursExternalNoProvision(2);
        report.setTotalWorkedHoursInternalNoProvision(10);
        report.setTotalWorkedHoursInternalProvision(8);

        assertEquals(200, report.getTotalWorkableHours());
        assertEquals(2, report.getTotalWorkedHoursExternalNoProvision(), 0.001);
        assertEquals(80, report.getTotalWorkedHoursExternalProvision(), 0.001);
        assertEquals(10, report.getTotalWorkedHoursInternalNoProvision(), 0.001);
        assertEquals(8, report.getTotalWorkedHoursInternalProvision(), 0.001);
        assertEquals(88, report.getTotalWorkedHoursProvision(), 0.001);
        assertEquals(12, report.getTotalWorkedHoursNoProvision(), 0.001);

        assertEquals(1, report.getTotalWorkedHoursExternalNoProvisionPcp(), 0.001);
        assertEquals(40, report.getTotalWorkedHoursExternalProvisionPcp(), 0.001);
        assertEquals(5, report.getTotalWorkedHoursInternalNoProvisionPcp(), 0.001);
        assertEquals(4, report.getTotalWorkedHoursInternalProvisionPcp(), 0.001);
        assertEquals(44, report.getTotalWorkedHoursProvisionPcp(), 0.001);
        assertEquals(6, report.getTotalWorkedHoursNoProvisionPcp(), 0.001);
    }
}