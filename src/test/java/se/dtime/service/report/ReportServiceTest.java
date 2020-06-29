package se.dtime.service.report;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import se.dtime.model.report.Report;
import se.dtime.model.report.UserReport;
import se.dtime.service.system.SystemProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;
    @Mock
    private Environment environment;

    @Before
    public void setUp() {
        when(environment.getProperty(SystemProperty.PROVISION_LIMIT_PROP)).thenReturn("0.9");
        reportService.setUp();
    }

    @Test
    public void calcProvisionHoursTest() {
        UserReport userReport = new UserReport();
        assertEquals(0, reportService.calcAndUpdateProvisionHours(0, userReport));

        userReport.setTotalTimeInternalProvision(120);
        assertEquals(0, reportService.calcAndUpdateProvisionHours(160, userReport));

        userReport.setTotalTimeInternalProvision(144);
        assertEquals(0, reportService.calcAndUpdateProvisionHours(160, userReport));

        userReport.setTotalTimeInternalProvision(145);
        assertEquals(1, reportService.calcAndUpdateProvisionHours(160, userReport));

        userReport.setTotalTimeInternalProvision(160);
        assertEquals(16, reportService.calcAndUpdateProvisionHours(160, userReport));

        userReport.setTotalTimeInternalProvision(144.1f);
        assertEquals(1, reportService.calcAndUpdateProvisionHours(160, userReport));

        userReport.setTotalTimeInternalProvision(144.5f);
        assertEquals(1, reportService.calcAndUpdateProvisionHours(160, userReport));

        userReport.setTotalTimeInternalProvision(144.7f);
        assertEquals(1, reportService.calcAndUpdateProvisionHours(160, userReport));
    }

    @Test
    public void calcStatisticsTest() {
        Report report = new Report(LocalDate.now(), LocalDate.now(), 200);
        List<UserReport> userReports = new ArrayList<>();
        UserReport userReport1 = new UserReport();
        userReport1.setTotalTimeInternalProvision(10);
        userReport1.setTotalTimeInternalNoProvision(5);
        userReport1.setTotalTimeExternalProvision(100);
        userReport1.setTotalTimeExternalNoProvision(10);

        UserReport userReport2 = new UserReport();
        userReport2.setTotalTimeInternalProvision(15);
        userReport2.setTotalTimeInternalNoProvision(10);
        userReport2.setTotalTimeExternalProvision(150);
        userReport2.setTotalTimeExternalNoProvision(20);

        userReports.add(userReport1);
        userReports.add(userReport2);

        reportService.calcStatistics(report, userReports);
        assertEquals(200, report.getWorkableHours());
        assertEquals(400, report.getTotalWorkableHours());
        assertEquals(320, report.getTotalWorkedHours(), 0.000001);
        assertEquals(275, report.getTotalWorkedHoursProvision(), 0.000001);
        assertEquals(45, report.getTotalWorkedHoursNoProvision(), 0.000001);
        assertEquals(250, report.getTotalWorkedHoursExternalProvision(), 0.000001);
        assertEquals(30, report.getTotalWorkedHoursExternalNoProvision(), 0.000001);
        assertEquals(25, report.getTotalWorkedHoursInternalProvision(), 0.000001);
        assertEquals(15, report.getTotalWorkedHoursInternalNoProvision(), 0.000001);

        assertEquals(80, report.getTotalWorkedHoursPcp(), 0.0001);
        assertEquals(68.75, report.getTotalWorkedHoursProvisionPcp(), 0.0001);
        assertEquals(11.25, report.getTotalWorkedHoursNoProvisionPcp(), 0.0001);
        assertEquals(62.5, report.getTotalWorkedHoursExternalProvisionPcp(), 0.0001);
        assertEquals(7.5, report.getTotalWorkedHoursExternalNoProvisionPcp(), 0.0001);
        assertEquals(6.25, report.getTotalWorkedHoursInternalProvisionPcp(), 0.0001);
        assertEquals(3.75, report.getTotalWorkedHoursInternalNoProvisionPcp(), 0.0001);
    }
}