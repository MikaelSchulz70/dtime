package se.dtime.service.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.timereport.Day;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.user.CurrentUserResolver;
import se.dtime.service.user.UserValidator;
import se.dtime.model.report.ReportView;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceGetUserReportTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private UserValidator userValidator;
    @Mock
    private CurrentUserResolver currentUserResolver;
    @Mock
    private CalendarService calendarService;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private CloseDateRepository closeDateRepository;
    @Mock
    private ReportValidator reportValidator;
    @Mock
    private ReportConverter reportConverter;

    @Test
    void getUserReport_passesResolvedUserIdToRepository() {
        LocalDate now = LocalDate.of(2024, 6, 15);
        when(calendarService.getNowDate()).thenReturn(now);

        UserPO resolved = new UserPO(42L);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(resolved);

        LocalDate from = LocalDate.of(2024, 6, 1);
        LocalDate to = LocalDate.of(2024, 6, 30);
        Day[] days = new Day[]{
                Day.builder().date(from).year(2024).month(6).day(1).build()
        };
        when(calendarService.getDays(from, to)).thenReturn(days);
        when(calendarService.calcWorkableHours(days)).thenReturn(160);
        when(reportRepository.getUserTaskReports(42L, from, to)).thenReturn(Collections.emptyList());

        var report = reportService.getUserReport(ReportView.MONTH);

        assertNotNull(report);
        verify(reportRepository).getUserTaskReports(eq(42L), eq(from), eq(to));
    }
}
