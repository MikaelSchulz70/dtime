package se.dtime.service.timereport;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.TimeReportDayPO;
import se.dtime.model.Project;
import se.dtime.model.timereport.*;
import se.dtime.service.project.ProjectConverter;
import se.dtime.service.user.UserConverter;
import se.dtime.model.timereport.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeReportConverterTest {
    private static int YEAR = 2018;
    private static int MONTH = 10;
    private static int FIRST_DAY_IN_MONTH = 1;
    private static int LAST_DAY_IN_MONTH = 31;

    @InjectMocks
    private TimeReportConverter timeReportConverter;
    @Mock
    private ProjectConverter projectConverter;
    @Mock
    private UserConverter userConverter;

    @Test
    public void testConvertTimeReportDayPO() {
        LocalDate date = LocalDate.of(YEAR, MONTH, 8);
        TimeReportDayPO timeReportDayPO = createTimeReportDayPO(11, 1, 2, 8.5f, false, date);

        Day days = Day.builder().date(date).build();

        TimeReportDay timeReportDay = timeReportConverter.convertToTimeReportDay(days, timeReportDayPO);
        assertEquals(11, timeReportDay.getId());
        assertEquals(1, timeReportDay.getIdAssignment());
        assertEquals(8.5f, timeReportDay.getTime(), 0.000001);
        assertEquals(date, timeReportDay.getDay().getDate());
    }

    @Test
    public void testConvertTimeReportDayPOFromUserAndProject() {
        LocalDate date = LocalDate.of(YEAR, MONTH, 8);

        Day day = Day.builder().date(date).build();

        TimeReportDay timeReportDay = timeReportConverter.convertToTimeReportDay(day, new AssignmentPO(2L));
        assertEquals(0, timeReportDay.getId());
        assertEquals(2, timeReportDay.getIdAssignment());
        assertNull(timeReportDay.getTime());
        assertEquals(date, timeReportDay.getDay().getDate());
    }

    @Test
    public void testConvertToTimeReport() {
        LocalDate fromDate = LocalDate.of(YEAR, MONTH, FIRST_DAY_IN_MONTH);
        LocalDate toDate = LocalDate.of(YEAR, MONTH, LAST_DAY_IN_MONTH);
        UserPO userPO = new UserPO(1L);

        List<TimeReportDayPO> timeReportDays = createTimeReportDays();
        Day[] days = createDays(fromDate, toDate);
        List<AssignmentPO> assignmentPOS = createAssignments();

        TimeReport timeReport = timeReportConverter.convertToTimeReport(days, userPO, timeReportDays, assignmentPOS);

        assertEquals(2, timeReport.getTimeReportProjectsInternal().size());
        assertEquals(2, timeReport.getTimeReportProjectsExternal().size());
        assertEquals(LAST_DAY_IN_MONTH, timeReport.getTimeReportProjectsInternal().get(0).getTimeReportDays().size());
        assertEquals(LAST_DAY_IN_MONTH, timeReport.getTimeReportProjectsInternal().get(1).getTimeReportDays().size());
        assertEquals(LAST_DAY_IN_MONTH, timeReport.getTimeReportProjectsExternal().get(0).getTimeReportDays().size());
        assertEquals(LAST_DAY_IN_MONTH, timeReport.getTimeReportProjectsExternal().get(1).getTimeReportDays().size());

        TimeReportProject timeReportProjectExternal1 = getTimeReportProjectByProjectId(timeReport.getTimeReportProjectsExternal(), 1);
        TimeReportProject timeReportProjectExternal2 = getTimeReportProjectByProjectId(timeReport.getTimeReportProjectsExternal(), 2);
        TimeReportProject timeReportProjectInternal3 = getTimeReportProjectByProjectId(timeReport.getTimeReportProjectsInternal(), 3);
        TimeReportProject timeReportProjectInternal4 = getTimeReportProjectByProjectId(timeReport.getTimeReportProjectsInternal(), 4);

        assertTrue(hasNoTimes(timeReportProjectExternal1, 3, 5, 7));
        assertTrue(hasTimeAtDate(timeReportProjectExternal1, LocalDate.of(YEAR, MONTH, 3)));
        assertTrue(hasTimeAtDate(timeReportProjectExternal1, LocalDate.of(YEAR, MONTH, 5)));
        assertTrue(hasTimeAtDate(timeReportProjectExternal1, LocalDate.of(YEAR, MONTH, 7)));

        assertTrue(hasTimeAtDate(timeReportProjectExternal2, LocalDate.of(YEAR, MONTH, 8)));
        assertTrue(hasTimeAtDate(timeReportProjectExternal2, LocalDate.of(YEAR, MONTH, 10)));
        assertTrue(hasNoTimes(timeReportProjectExternal2, 8, 10));

        assertTrue(hasTimeAtDate(timeReportProjectInternal3, LocalDate.of(YEAR, MONTH, 3)));
        assertTrue(hasNoTimes(timeReportProjectInternal3, 3));

        assertTrue(hasNoTimes(timeReportProjectInternal4));
    }

    @Test
    public void testIsWithinMonth() {
        Day[] days = new Day[1];
        days[0] = Day.builder().withinCurrentMonth(true).build();
        assertTrue(timeReportConverter.isWithinMonth(days));

        days[0] = Day.builder().withinCurrentMonth(false).build();
        assertFalse(timeReportConverter.isWithinMonth(days));

        days = new Day[2];
        days[0] = Day.builder().withinCurrentMonth(true).build();
        days[1] = Day.builder().withinCurrentMonth(true).build();
        assertTrue(timeReportConverter.isWithinMonth(days));

        days[0] = Day.builder().withinCurrentMonth(false).build();
        days[1] = Day.builder().withinCurrentMonth(true).build();
        assertTrue(timeReportConverter.isWithinMonth(days));

        days[0] = Day.builder().withinCurrentMonth(true).build();
        days[1] = Day.builder().withinCurrentMonth(false).build();
        assertTrue(timeReportConverter.isWithinMonth(days));

        days[0] = Day.builder().withinCurrentMonth(false).build();
        days[1] = Day.builder().withinCurrentMonth(false).build();
        assertFalse(timeReportConverter.isWithinMonth(days));
    }

    @Test
    public void convertToUserVacationsTest() {
        List<UserPO> userPOs = new ArrayList<>();
        UserPO userPO1 = new UserPO(1L);
        userPO1.setFirstName("Kalle");
        userPO1.setLastName("Anka");
        UserPO userPO2 = new UserPO(2L);
        userPO2.setFirstName("Kajsa");
        userPO2.setLastName("Anka");
        UserPO userPO3 = new UserPO(3L);
        userPO3.setFirstName("Joakim");
        userPO3.setLastName("von Anka");
        userPOs.add(userPO1);
        userPOs.add(userPO2);
        userPOs.add(userPO3);

        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);

        Day[] days = new Day[2];
        days[0] = Day.builder().date(now).build();
        days[1] = Day.builder().date(tomorrow).build();

        List<TimeReportDayPO> timeReportDayPOS = new ArrayList<>();

        TimeReportDayPO timeReportDayPO1 = new TimeReportDayPO();
        AssignmentPO assignmentPO1 = new AssignmentPO();
        assignmentPO1.setUser(userPO1);
        timeReportDayPO1.setAssignment(assignmentPO1);
        timeReportDayPO1.setDate(now);

        TimeReportDayPO timeReportDayPO2 = new TimeReportDayPO();
        AssignmentPO assignmentPO2 = new AssignmentPO();
        assignmentPO2.setUser(userPO1);
        timeReportDayPO2.setAssignment(assignmentPO2);
        timeReportDayPO2.setDate(tomorrow);

        TimeReportDayPO timeReportDayPO3 = new TimeReportDayPO();
        AssignmentPO assignmentPO3 = new AssignmentPO();
        assignmentPO3.setUser(userPO2);
        timeReportDayPO3.setAssignment(assignmentPO3);
        timeReportDayPO3.setDate(now);

        timeReportDayPOS.add(timeReportDayPO1);
        timeReportDayPOS.add(timeReportDayPO2);
        timeReportDayPOS.add(timeReportDayPO3);

        List<UserVacation> vacations = timeReportConverter.convertToUserVacations(userPOs, timeReportDayPOS, days);

        assertEquals(3, vacations.size());

        UserVacation userVacation1 = vacations.get(0);
        assertEquals("Kalle Anka", userVacation1.getName());
        assertEquals(2, userVacation1.getNoVacationDays());
        assertEquals(2, userVacation1.getVacationsDays().length);
        assertTrue(userVacation1.getVacationsDays()[0].isVacation());
        assertTrue(userVacation1.getVacationsDays()[1].isVacation());

        UserVacation userVacation2 = vacations.get(1);
        assertEquals("Kajsa Anka", userVacation2.getName());
        assertEquals(1, userVacation2.getNoVacationDays());
        assertEquals(2, userVacation2.getVacationsDays().length);
        assertTrue(userVacation2.getVacationsDays()[0].isVacation());
        assertFalse(userVacation2.getVacationsDays()[1].isVacation());

        UserVacation userVacation3 = vacations.get(2);
        assertEquals("Joakim von Anka", userVacation3.getName());
        assertEquals(0, userVacation3.getNoVacationDays());
        assertEquals(2, userVacation3.getVacationsDays().length);
        assertFalse(userVacation3.getVacationsDays()[0].isVacation());
        assertFalse(userVacation3.getVacationsDays()[1].isVacation());
    }

    private Day[] createDays(LocalDate fromDate, LocalDate toDate) {
        List<Day> days = new ArrayList<>();
        LocalDate date = fromDate;
        while(date.isBefore(toDate) || date.isEqual(toDate)) {
            days.add(Day.builder().date(date).build());
            date = date.plusDays(1);
        }

        return days.toArray(new Day[days.size()]);
    }

    private TimeReportProject getTimeReportProjectByProjectId(List<TimeReportProject> timeReportProjects, int id) {
        return timeReportProjects.stream().filter(t -> t.getProject().getId() == id).findFirst().get();
    }

    private List<TimeReportDayPO> createTimeReportDays() {
        List<TimeReportDayPO> timeReportDays = new ArrayList<>();
        LocalDate date = LocalDate.of(YEAR, MONTH, 3);
        timeReportDays.add(createTimeReportDayPO(1, 1,1, 8f, false, date));

        date = LocalDate.of(YEAR, MONTH, 5);
        timeReportDays.add(createTimeReportDayPO(2, 1,1, 8f, false, date));

        date = LocalDate.of(YEAR, MONTH, 7);
        timeReportDays.add(createTimeReportDayPO(3, 1,1, 8f, false, date));

        date = LocalDate.of(YEAR, MONTH, 8);
        timeReportDays.add(createTimeReportDayPO(4, 1,2, 8f, false, date));

        date = LocalDate.of(YEAR, MONTH, 10);
        timeReportDays.add(createTimeReportDayPO(5, 1,2, 8f, false, date));

        date = LocalDate.of(YEAR, MONTH, 3);
        timeReportDays.add(createTimeReportDayPO(6, 1,3, 8f, true, date));

        return timeReportDays;
    }

    private List<AssignmentPO> createAssignments() {
        List<AssignmentPO> assignmentPOS = new ArrayList<>();
        assignmentPOS.add(createAssignment(1,1, 2, false));
        assignmentPOS.add(createAssignment(1, 1, 4, true));
        return assignmentPOS;
    }

    private AssignmentPO createAssignment(long idAssignment, long idUser, long idProject, boolean internalProject) {
        AssignmentPO assignmentPO = new AssignmentPO(idAssignment);
        assignmentPO.setUser(new UserPO(idUser));
        assignmentPO.setProject(createProjectPO(idProject, internalProject));
        return assignmentPO;
    }

    private TimeReportDayPO createTimeReportDayPO(long id, long idUser, long idProject, float time, boolean internalProject, LocalDate date) {
        TimeReportDayPO timeReportDayPO = new TimeReportDayPO();
        AssignmentPO assignmentPO = new AssignmentPO(1);
        timeReportDayPO.setId(id);
        assignmentPO.setUser(new UserPO(idUser));
        assignmentPO.setProject(createProjectPO(idProject, internalProject));
        timeReportDayPO.setTime(time);
        timeReportDayPO.setDate(date);
        timeReportDayPO.setAssignment(assignmentPO);
        return timeReportDayPO;
    }

    private ProjectPO createProjectPO(long id, boolean internalProject) {
        ProjectPO projectPO = new ProjectPO(id);
        projectPO.setName("ProjName");
        projectPO.setInternal(internalProject);
        when(projectConverter.toModel(projectPO)).thenReturn(Project.builder().id(id).name("ProjName").build());
        return projectPO;
    }

    private boolean hasNoTimes(TimeReportProject timeReportProject, int... daysToExclude) {
        for (int day = 1; day <= LAST_DAY_IN_MONTH; day++) {
            if (ArrayUtils.contains(daysToExclude, day)) {
                continue;
            }

            if (timeReportProject.getTimeReportDays().get(day - 1).getTime() != null) {
                return false;
            }
        }

        return true;
    }

    private boolean hasTimeAtDate(TimeReportProject timeReportProject, LocalDate date) {
        TimeReportDay timeReportDay = timeReportProject.getTimeReportDays().stream().filter(t -> t.getDay().getDate().equals(date)).findFirst().get();
        return timeReportDay.getTime() != null;
    }
}