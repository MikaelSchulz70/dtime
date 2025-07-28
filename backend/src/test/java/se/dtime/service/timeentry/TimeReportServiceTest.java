package se.dtime.service.timeentry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ReportDates;
import se.dtime.model.UserExt;
import se.dtime.model.timereport.*;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TimeEntryRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TimeReportServiceTest {
    @InjectMocks
    private TimeEntryService timeReportService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private TimeReportConverter timeReportConverter;
    @Mock
    private TaskContributorRepository taskContributorRepository;
    @Mock
    private CalendarService calendarService;
    @Mock
    private CloseDateRepository closeDateRepository;

    @BeforeEach
    public void setUp() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        UserExt userExt = new UserExt("name", "pwd", authorities, 1, "", "");

        SecurityContextHolder.setContext(createSecurityContext(userExt));
    }

    @Test
    public void getPreviousDateTest() {
        LocalDate date = LocalDate.of(2018, 1, 17);
        assertEquals(LocalDate.of(2018, 1, 10), timeReportService.getPreviousDate(TimeReportView.WEEK, date));
        assertEquals(LocalDate.of(2017, 12, 17), timeReportService.getPreviousDate(TimeReportView.MONTH, date));
    }

    @Test
    public void getNextDateTest() {
        LocalDate date = LocalDate.of(2018, 7, 16);
        assertEquals(LocalDate.of(2018, 7, 23), timeReportService.getNextDate(TimeReportView.WEEK, date));
        assertEquals(LocalDate.of(2018, 8, 16), timeReportService.getNextDate(TimeReportView.MONTH, date));
    }

    @Test
    public void getReportDatesTest() {
        LocalDate date = LocalDate.of(2018, 11, 28);

        when(calendarService.getClosestMonday(date)).thenReturn(LocalDate.of(2018, 11, 26));
        ReportDates reportDates = timeReportService.getReportDates(TimeReportView.WEEK, date);
        assertEquals(LocalDate.of(2018, 11, 26), reportDates.getFromDate());
        assertEquals(LocalDate.of(2018, 12, 2), reportDates.getToDate());

        reportDates = timeReportService.getReportDates(TimeReportView.MONTH, date);
        assertEquals(LocalDate.of(2018, 11, 1), reportDates.getFromDate());
        assertEquals(LocalDate.of(2018, 11, 30), reportDates.getToDate());
    }

    @Test
    public void getClosedMonthsNoClosedTest() {
        Day[] days = createDays();
        UserPO userPO = new UserPO(1L);
        LocalDate date1 = LocalDate.of(2018, 11, 1);
        LocalDate date2 = LocalDate.of(2018, 12, 1);
        when(closeDateRepository.findByUserAndDate(userPO, date1)).thenReturn(null);
        when(closeDateRepository.findByUserAndDate(userPO, date2)).thenReturn(null);
        Set<LocalDate> closedMonths = timeReportService.getClosedMonths(days, userPO);
        assertEquals(0, closedMonths.size());

        CloseDatePO closeDatePO1 = new CloseDatePO();
        closeDatePO1.setUser(userPO);
        closeDatePO1.setDate(date1);
        when(closeDateRepository.findByUserAndDate(userPO, date1)).thenReturn(closeDatePO1);
        closedMonths = timeReportService.getClosedMonths(days, userPO);
        assertEquals(1, closedMonths.size());

        CloseDatePO closeDatePO2 = new CloseDatePO();
        closeDatePO2.setUser(userPO);
        closeDatePO2.setDate(date2);
        when(closeDateRepository.findByUserAndDate(userPO, date2)).thenReturn(closeDatePO2);
        closedMonths = timeReportService.getClosedMonths(days, userPO);
        assertEquals(2, closedMonths.size());
    }

    @Test
    public void updateClosedDaysTest() {
        List<TimeReportTask> timeReportTasks = new ArrayList<>();

        // First task with 3 entries
        TimeEntry day1 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 9, 22)).build()).build();
        TimeEntry day2 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 10, 22)).build()).build();
        TimeEntry day3 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 10, 23)).build()).build();
        List<TimeEntry> timeReportDays1 = new ArrayList<>();
        timeReportDays1.add(day1);
        timeReportDays1.add(day2);
        timeReportDays1.add(day3);
        TimeReportTask timeReportTask1 = TimeReportTask.builder().timeEntries(timeReportDays1).build();
        timeReportTasks.add(timeReportTask1);

        // Second task with 2 entries
        TimeEntry day4 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 9, 22)).build()).build();
        TimeEntry day5 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 10, 1)).build()).build();
        List<TimeEntry> timeReportDays2 = new ArrayList<>();
        timeReportDays2.add(day4);
        timeReportDays2.add(day5);
        TimeReportTask timeReportTask2 = TimeReportTask.builder().timeEntries(timeReportDays2).build();

        List<TimeEntry> timeReportDays3 = new ArrayList<>();
        TimeEntry day6 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 11, 1)).build()).build();
        TimeEntry day7 = TimeEntry.builder().day(Day.builder().date(LocalDate.of(2018, 12, 30)).build()).build();
        timeReportDays3.add(day6);
        timeReportDays3.add(day7);
        TimeReportTask timeReportTask3 = TimeReportTask.builder().timeEntries(timeReportDays3).build();

        timeReportTasks.add(timeReportTask2);
        timeReportTasks.add(timeReportTask3);

        TimeReport timeReport = TimeReport.builder().
                timeReportTasks(timeReportTasks).
                build();

        LocalDate date1 = LocalDate.of(2018, 10, 30);
        LocalDate date2 = LocalDate.of(2018, 11, 1);
        Set<LocalDate> closedMonth = new HashSet<>();
        closedMonth.add(date1);
        closedMonth.add(date2);

        when(calendarService.getSystemStartDate()).thenReturn(LocalDate.of(2018, 1, 1));
        timeReportService.updateClosedDays(timeReport, closedMonth);

        assertFalse(timeReportTasks.get(0).getTimeEntries().get(0).isClosed());
        assertTrue(timeReportTasks.get(0).getTimeEntries().get(1).isClosed());
        assertTrue(timeReportTasks.get(0).getTimeEntries().get(2).isClosed());

        assertFalse(timeReportTasks.get(1).getTimeEntries().get(0).isClosed());
        assertTrue(timeReportTasks.get(1).getTimeEntries().get(1).isClosed());

        assertTrue(timeReportTasks.get(2).getTimeEntries().get(0).isClosed());
        assertFalse(timeReportTasks.get(2).getTimeEntries().get(1).isClosed());
    }

    private Day[] createDays() {
        Day[] days = new Day[3];
        days[0] = Day.builder().date(LocalDate.of(2018, 11, 30)).year(2018).month(11).day(30).build();
        days[1] = Day.builder().date(LocalDate.of(2018, 12, 1)).year(2018).month(12).day(1).build();
        days[2] = Day.builder().date(LocalDate.of(2018, 12, 1)).year(2018).month(12).day(2).build();
        return days;
    }

    private SecurityContext createSecurityContext(UserExt userExt) {
        return new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return userExt;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return false;
                    }

                    @Override
                    public void setAuthenticated(boolean b) throws IllegalArgumentException {

                    }

                    @Override
                    public String getName() {
                        return null;
                    }
                };
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        };
    }
}