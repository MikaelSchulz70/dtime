package se.dtime.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.ReportDates;
import se.dtime.model.report.UnclosedUserReport;
import se.dtime.model.report.UserReport;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.UserRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.period.PeriodResolver;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeReportStatusServiceTest {

    @Mock
    private CalendarService calendarService;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private CloseDateRepository closeDateRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReportConverter reportConverter;
    @Mock
    private PeriodResolver periodResolver;

    private TimeReportStatusService service;

    @BeforeEach
    void setUp() {
        service = new TimeReportStatusService(
                calendarService,
                reportRepository,
                closeDateRepository,
                userRepository,
                reportConverter,
                periodResolver
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "n/a")
        );
    }

    @Test
    void shouldIncludeAllActiveUsersWithOpenReportsByDefault() {
        ReportDates reportDates = new ReportDates(LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31));
        when(periodResolver.resolveMonth(2025, 5)).thenReturn(reportDates);
        when(calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate())).thenReturn(new se.dtime.model.timereport.Day[0]);
        when(calendarService.calcWorkableHours(new se.dtime.model.timereport.Day[0])).thenReturn(160);

        UserReport reportedUser = new UserReport();
        reportedUser.setUserId(1L);
        reportedUser.setFullName("Alice");
        reportedUser.setEmail("alice@example.com");
        reportedUser.setTotalTime(BigDecimal.TEN);
        when(reportRepository.getUserTaskReports(reportDates.getFromDate(), reportDates.getToDate()))
                .thenReturn(List.of(reportedUser));

        UserPO activeUser = new UserPO(2L);
        activeUser.setDisplayName("Bob Example");
        activeUser.setEmail("bob@example.com");
        activeUser.setActivationStatus(ActivationStatus.ACTIVE);
        when(userRepository.findByActivationStatusOrderByDisplayNameAsc(ActivationStatus.ACTIVE))
                .thenReturn(List.of(activeUser));

        when(closeDateRepository.findByUserAndDate(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        UnclosedUserReport report = service.getUnclosedUsersForMonth(2025, 5);

        assertThat(report.getUnclosedUsers()).hasSize(2);
        assertThat(report.getUnclosedUsers()).allMatch(user -> !user.isClosed());
    }
}
