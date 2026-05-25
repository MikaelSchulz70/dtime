package se.dtime.service.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ReportDates;
import se.dtime.model.timereport.Day;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.period.PeriodResolver;
import se.dtime.service.user.CurrentUserResolver;
import se.dtime.service.user.UserValidator;

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
    private ReportRepository reportRepository;
    @Mock
    private CloseDateRepository closeDateRepository;
    @Mock
    private ReportValidator reportValidator;
    @Mock
    private ReportConverter reportConverter;
    @Mock
    private PeriodResolver periodResolver;
    @Mock
    private CalendarService calendarService;

    @Test
    void getUserReportForMonth_passesResolvedUserIdToRepository() {
        LocalDate from = LocalDate.of(2025, 6, 1);
        LocalDate to = LocalDate.of(2025, 6, 30);
        when(periodResolver.resolveMonth(2025, 6)).thenReturn(new ReportDates(from, to));

        UserPO resolved = new UserPO(42L);
        when(currentUserResolver.resolveCurrentUser()).thenReturn(resolved);

        Day[] days = new Day[]{
                Day.builder().date(from).year(2025).month(6).day(1).build()
        };
        when(calendarService.getDays(from, to)).thenReturn(days);
        when(calendarService.calcWorkableHours(days)).thenReturn(160);
        when(reportRepository.getUserTaskReports(42L, from, to)).thenReturn(Collections.emptyList());

        var report = reportService.getUserReportForMonth(2025, 6);

        assertNotNull(report);
        verify(reportRepository).getUserTaskReports(eq(42L), eq(from), eq(to));
    }
}
