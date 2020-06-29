package se.dtime.model.report;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserReportTest {

    @Test
    public void getTotalTime() {
        UserReport userReport = new UserReport();
        userReport.setTotalTimeExternalNoProvision(10.5f);
        userReport.setTotalTimeExternalProvision(40);
        userReport.setTotalTimeInternalNoProvision(15.2f);
        userReport.setTotalTimeInternalProvision(10);

        assertEquals(75.7, userReport.getTotalTime(), 0.00001);
        assertEquals(50, userReport.getTotalTimeProvision(), 0.0001);
    }
}