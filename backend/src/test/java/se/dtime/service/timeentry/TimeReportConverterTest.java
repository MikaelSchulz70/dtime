package se.dtime.service.timeentry;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.Task;
import se.dtime.model.timereport.*;
import se.dtime.service.task.TaskConverter;
import se.dtime.service.user.UserConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TimeReportConverterTest {
    private static final int YEAR = 2018;
    private static final int MONTH = 10;
    private static final int LAST_DAY_IN_MONTH = 31;

    @InjectMocks
    private TimeReportConverter timeReportConverter;
    @Mock
    private TaskConverter taskConverter;
    @Mock
    private UserConverter userConverter;

    @Test
    public void testConvertTimeReportDayPO() {
        LocalDate date = LocalDate.of(YEAR, MONTH, 8);
        TimeEntryPO timeEntryPO = createTimeEntryPO(11, 1, 2, 8.5f, date);

        Day days = Day.builder().date(date).build();

        TimeEntry timeEntry = timeReportConverter.convertToTimeReportDay(days, timeEntryPO);
        assertEquals(11, timeEntry.getId());
        assertEquals(1, timeEntry.getTaskContributorId());
        assertEquals(8.5f, timeEntry.getTime(), 0.000001);
        assertEquals(date, timeEntry.getDay().getDate());
    }

    @Test
    public void testConvertTimeReportDayPOFromUserAndTask() {
        LocalDate date = LocalDate.of(YEAR, MONTH, 8);

        Day day = Day.builder().date(date).build();

        TimeEntry timeEntry = timeReportConverter.convertToTimeReportDay(day, new TaskContributorPO(2L));
        assertEquals(0, timeEntry.getId());
        assertEquals(2, timeEntry.getTaskContributorId());
        assertNull(timeEntry.getTime());
        assertEquals(date, timeEntry.getDay().getDate());
    }

    @Test
    public void testConvertToTimeReport() {
        int FIRST_DAY_IN_MONTH = 1;
        LocalDate fromDate = LocalDate.of(YEAR, MONTH, FIRST_DAY_IN_MONTH);
        LocalDate toDate = LocalDate.of(YEAR, MONTH, LAST_DAY_IN_MONTH);
        UserPO userPO = new UserPO(1L);

        List<TimeEntryPO> timeEntryPOS = createTimeReportDays();
        Day[] days = createDays(fromDate, toDate);
        List<TaskContributorPO> taskContributorPOS = createTaskContributors();

        Task task = Task.builder().name("").build();
        when(taskConverter.toModel(any(TaskPO.class))).thenReturn(task);
        TimeReport timeReport = timeReportConverter.convertToTimeReport(days, userPO, timeEntryPOS, taskContributorPOS);

        assertEquals(4, timeReport.getTimeReportTasks().size());
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

        List<TimeEntryPO> timeReportDayPOS = new ArrayList<>();

        TimeEntryPO timeReportDayPO1 = new TimeEntryPO();
        TaskContributorPO taskContributorPO1 = new TaskContributorPO();
        taskContributorPO1.setUser(userPO1);
        timeReportDayPO1.setTaskContributor(taskContributorPO1);
        timeReportDayPO1.setDate(now);

        TimeEntryPO timeReportDayPO2 = new TimeEntryPO();
        TaskContributorPO taskContributorPO2 = new TaskContributorPO();
        taskContributorPO2.setUser(userPO1);
        timeReportDayPO2.setTaskContributor(taskContributorPO2);
        timeReportDayPO2.setDate(tomorrow);

        TimeEntryPO timeReportDayPO3 = new TimeEntryPO();
        TaskContributorPO taskContributorPO3 = new TaskContributorPO();
        taskContributorPO3.setUser(userPO2);
        timeReportDayPO3.setTaskContributor(taskContributorPO3);
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
        while (date.isBefore(toDate) || date.isEqual(toDate)) {
            days.add(Day.builder().date(date).build());
            date = date.plusDays(1);
        }

        return days.toArray(new Day[0]);
    }

    private List<TimeEntryPO> createTimeReportDays() {
        List<TimeEntryPO> timeReportDays = new ArrayList<>();
        LocalDate date = LocalDate.of(YEAR, MONTH, 3);
        timeReportDays.add(createTimeEntryPO(1, 1, 1, 8f, date));

        date = LocalDate.of(YEAR, MONTH, 5);
        timeReportDays.add(createTimeEntryPO(2, 1, 1, 8f, date));

        date = LocalDate.of(YEAR, MONTH, 7);
        timeReportDays.add(createTimeEntryPO(3, 1, 1, 8f, date));

        date = LocalDate.of(YEAR, MONTH, 8);
        timeReportDays.add(createTimeEntryPO(4, 1, 2, 8f, date));

        date = LocalDate.of(YEAR, MONTH, 10);
        timeReportDays.add(createTimeEntryPO(5, 1, 2, 8f, date));

        date = LocalDate.of(YEAR, MONTH, 3);
        timeReportDays.add(createTimeEntryPO(6, 1, 3, 8f, date));

        return timeReportDays;
    }

    private List<TaskContributorPO> createTaskContributors() {
        List<TaskContributorPO> TaskContributorPOS = new ArrayList<>();
        TaskContributorPOS.add(createTaskContributor(1, 1, 2));
        TaskContributorPOS.add(createTaskContributor(1, 1, 4));
        return TaskContributorPOS;
    }

    private TaskContributorPO createTaskContributor(long taskContributorId, long userId, long taskId) {
        TaskContributorPO taskContributorPO = new TaskContributorPO(taskContributorId);
        taskContributorPO.setUser(new UserPO(userId));
        taskContributorPO.setTask(createTaskPO(taskId));
        return taskContributorPO;
    }

    private TimeEntryPO createTimeEntryPO(long id, long userId, long taskId, float time, LocalDate date) {
        TimeEntryPO timeEntryPO = new TimeEntryPO();
        TaskContributorPO taskContributorPO = new TaskContributorPO(1);
        timeEntryPO.setId(id);
        taskContributorPO.setUser(new UserPO(userId));
        taskContributorPO.setTask(createTaskPO(taskId));
        timeEntryPO.setTime(time);
        timeEntryPO.setDate(date);
        timeEntryPO.setTaskContributor(taskContributorPO);
        return timeEntryPO;
    }

    private TaskPO createTaskPO(long id) {
        TaskPO taskPO = new TaskPO(id);
        taskPO.setName("TaskName");
        return taskPO;
    }

    private boolean hasNoTimes(TimeReportTask timeReportTask, int... daysToExclude) {
        for (int day = 1; day <= LAST_DAY_IN_MONTH; day++) {
            if (ArrayUtils.contains(daysToExclude, day)) {
                continue;
            }

            if (timeReportTask.getTimeEntries().get(day - 1).getTime() != null) {
                return false;
            }
        }

        return true;
    }

    private boolean hasTimeAtDate(TimeReportTask timeReportTask, LocalDate date) {
        TimeEntry timeEntry = timeReportTask.getTimeEntries().stream().filter(t -> t.getDay().getDate().equals(date)).findFirst().get();
        return timeEntry.getTime() != null;
    }
}