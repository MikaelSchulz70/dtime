package se.dtime.service.timereport;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ReportDates;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.timereport.*;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TimeReportRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.model.timereport.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeReportServiceTest {
    @InjectMocks
    private TimeReportService timeReportService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeReportRepository timeReportRepository;
    @Mock
    private TimeReportConverter timeReportConverter;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private CalendarService calendarService;
    @Mock
    private CloseDateRepository closeDateRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        UserExt userExt = new UserExt("name", "pwd", authorities, 1, "", "");

        SecurityContextHolder.setContext(createSecurityContext(userExt));
    }

    @Test
    public void testGetPreviousNotLoggedIn() {
        SecurityContextHolder.setContext(createSecurityContext(null));
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("user.not.logged.in");

        timeReportService.getPreviousTimeReport(TimeReportView.DAY, LocalDate.now());
    }

    @Test
    public void testGetNextNotLoggedIn() {
        SecurityContextHolder.setContext(createSecurityContext(null));
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("user.not.logged.in");

        timeReportService.getNextTimeReport(TimeReportView.DAY, LocalDate.now());
    }

    @Test
    public void getPreviousDateTest() {
        LocalDate date = LocalDate.of(2018, 1 ,17);
        assertEquals(LocalDate.of(2018, 1, 16), timeReportService.getPreviousDate(TimeReportView.DAY, date));
        assertEquals(LocalDate.of(2018, 1, 10), timeReportService.getPreviousDate(TimeReportView.WEEK, date));
        assertEquals(LocalDate.of(2017, 12, 17), timeReportService.getPreviousDate(TimeReportView.MONTH, date));
    }

    @Test
    public void getNextDateTest() {
        LocalDate date = LocalDate.of(2018, 7 ,16);
        assertEquals(LocalDate.of(2018, 7, 17), timeReportService.getNextDate(TimeReportView.DAY, date));
        assertEquals(LocalDate.of(2018, 7, 23), timeReportService.getNextDate(TimeReportView.WEEK, date));
        assertEquals(LocalDate.of(2018, 8, 16), timeReportService.getNextDate(TimeReportView.MONTH, date));
    }

    @Test
    public void getReportDatesTest() {
        LocalDate date = LocalDate.of(2018, 11 ,28);

        ReportDates reportDates = timeReportService.getReportDates(TimeReportView.DAY, date);
        assertEquals(LocalDate.of(2018, 11, 28), reportDates.getFromDate());
        assertEquals(LocalDate.of(2018, 11, 28), reportDates.getToDate());

        when(calendarService.getClosestMonday(date)).thenReturn(LocalDate.of(2018, 11, 26));
        reportDates = timeReportService.getReportDates(TimeReportView.WEEK, date);
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
        List<TimeReportProject> timeReportProjectsExt = new ArrayList<>();
        TimeReportDay day1 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 9, 22)).build()).build();
        TimeReportDay day2 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 10, 22)).build()).build();
        TimeReportDay day3 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 10, 23)).build()).build();
        List<TimeReportDay> timeReportDays1 = new ArrayList<>();
        timeReportDays1.add(day1);
        timeReportDays1.add(day2);
        timeReportDays1.add(day3);
        TimeReportProject timeReportProject1 = TimeReportProject.builder().timeReportDays(timeReportDays1).build();
        timeReportProjectsExt.add(timeReportProject1);

        List<TimeReportProject> timeReportProjectsInt = new ArrayList<>();
        TimeReportDay day4 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 9, 22)).build()).build();
        TimeReportDay day5 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 10, 1)).build()).build();
        List<TimeReportDay> timeReportDays2 = new ArrayList<>();
        timeReportDays2.add(day4);
        timeReportDays2.add(day5);
        TimeReportProject timeReportProject2 = TimeReportProject.builder().timeReportDays(timeReportDays2).build();

        List<TimeReportDay> timeReportDays3 = new ArrayList<>();
        TimeReportDay day6 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 11, 1)).build()).build();
        TimeReportDay day7 = TimeReportDay.builder().day(Day.builder().date(LocalDate.of(2018, 12, 30)).build()).build();
        timeReportDays3.add(day6);
        timeReportDays3.add(day7);
        TimeReportProject timeReportProject3 = TimeReportProject.builder().timeReportDays(timeReportDays3).build();

        timeReportProjectsInt.add(timeReportProject2);
        timeReportProjectsInt.add(timeReportProject3);

        TimeReport timeReport = TimeReport.builder().
                timeReportProjectsExternal(timeReportProjectsExt).
                timeReportProjectsInternal(timeReportProjectsInt).
                build();

        LocalDate date1 = LocalDate.of(2018, 10, 30);
        LocalDate date2 = LocalDate.of(2018, 11, 1);
        Set<LocalDate> closedMonth = new HashSet<>();
        closedMonth.add(date1);
        closedMonth.add(date2);

        when(calendarService.getSystemStartDate()).thenReturn(LocalDate.of(2018, 1, 1));
        timeReportService.updateClosedDays(timeReport, closedMonth);

        assertFalse(timeReportProjectsExt.get(0).getTimeReportDays().get(0).isClosed());
        assertTrue(timeReportProjectsExt.get(0).getTimeReportDays().get(1).isClosed());
        assertTrue(timeReportProjectsExt.get(0).getTimeReportDays().get(2).isClosed());

        assertFalse(timeReportProjectsInt.get(0).getTimeReportDays().get(0).isClosed());
        assertTrue(timeReportProjectsInt.get(0).getTimeReportDays().get(1).isClosed());

        assertTrue(timeReportProjectsInt.get(1).getTimeReportDays().get(0).isClosed());
        assertFalse(timeReportProjectsInt.get(1).getTimeReportDays().get(1).isClosed());
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