package se.dtime.service.report;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.model.report.Report;
import se.dtime.model.report.UserReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Test
    public void calcStatisticsTest() {
        Report report = new Report(LocalDate.now(), LocalDate.now(), 200);
        List<UserReport> userReports = new ArrayList<>();
        UserReport userReport1 = new UserReport();
        userReport1.setTotalTime(BigDecimal.valueOf(10));

        UserReport userReport2 = new UserReport();
        userReport2.setTotalTime(BigDecimal.valueOf(15));

        userReports.add(userReport1);
        userReports.add(userReport2);

        reportService.calcStatistics(report, userReports);
        assertEquals(200, report.getWorkableHours());
        assertEquals(400, report.getTotalWorkableHours());
    }
}