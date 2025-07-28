package se.dtime.model.report;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportTest {

    @Test
    public void testCalculations() {
        Report report = new Report(LocalDate.now(), LocalDate.now(), 200);
        report.setTotalWorkableHours(200);

        assertEquals(200, report.getTotalWorkableHours());
    }
}