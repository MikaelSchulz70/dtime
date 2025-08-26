package se.dtime.service.timeentry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.timereport.*;
import se.dtime.service.BaseConverter;
import se.dtime.service.task.TaskConverter;
import se.dtime.service.user.UserConverter;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeReportConverter extends BaseConverter {

    @Autowired
    private TaskConverter taskConverter;
    @Autowired
    private UserConverter userConverter;

    TimeReport convertToTimeReport(Day[] days, UserPO userPO, List<TimeEntryPO> timeEntryPOS, List<TaskContributorPO> participations) {
        TimeReport timeReport = TimeReport.builder().
                days(days).
                user(userConverter.toModel(userPO)).
                build();

        Map<Long, TaskContributorPO> taskContributorPOMap = createTaskContributorMap(timeEntryPOS, participations);
        Set<TaskPO> taskPOS = participations.stream().map(TaskContributorPO::getTask).collect(Collectors.toSet());

        timeReport.setTimeReportTasks(convertToTimeReportTask(days, taskContributorPOMap, timeEntryPOS, taskPOS));

        double totalTime = timeReport.getTimeReportTasks().stream().mapToDouble(TimeReportTask::getTotalTime).sum();
        timeReport.setTotalTime((float) totalTime);

        return timeReport;
    }

    private List<TimeReportTask> convertToTimeReportTask(Day[] days, Map<Long, TaskContributorPO> taskContributorMap, List<TimeEntryPO> timeEntryPOS, Set<TaskPO> taskPOS) {
        List<TimeReportTask> timeReportTasks = new ArrayList<>();

        Set<TaskPO> taskPOSet = mergeCategories(timeEntryPOS, taskPOS);

        for (TaskPO taskPO : taskPOSet) {
            TimeReportTask timeReportTask = TimeReportTask.builder().
                    task(taskConverter.toModel(taskPO)).
                    timeEntries(new ArrayList<>()).
                    totalTime(0.0f).
                    build();

            timeReportTasks.add(timeReportTask);

            for (Day day : days) {
                TimeEntry timeEntry = timeEntryPOS.stream().
                        filter(t -> t.getTaskContributor().getTask().getId().equals(taskPO.getId()) && day.getDate().equals(t.getDate())).
                        findFirst().map(te -> convertToTimeReportDay(day, te)).
                        orElse(convertToTimeReportDay(day, taskContributorMap.get(taskPO.getId())));

                timeReportTask.setTotalTime(timeReportTask.getTotalTime() + (timeEntry.getTime() != null ? timeEntry.getTime() : 0f));
                timeReportTask.getTimeEntries().add(timeEntry);
            }
        }

        timeReportTasks.sort((TimeReportTask t1, TimeReportTask t2) -> t1.getTask().getName().compareTo(t2.getTask().getName()));

        return timeReportTasks;
    }

    TimeEntry convertToTimeReportDay(Day day, TaskContributorPO taskContributorPO) {
        return TimeEntry.builder().
                id(0).
                taskContributorId(taskContributorPO.getId()).
                time(null).
                day(day).
                build();
    }

    TimeEntry convertToTimeReportDay(Day day, TimeEntryPO timeEntryPO) {
        return TimeEntry.builder().
                id(timeEntryPO.getId()).
                taskContributorId(timeEntryPO.getTaskContributor().getId()).
                time(timeEntryPO.getTime()).
                day(day).
                build();

    }

    public TimeEntryPO toPO(TimeEntry timeReport) {
        TimeEntryPO timeEntryPO = new TimeEntryPO();
        timeEntryPO.setTime(timeReport.getTime() == null ? 0f : timeReport.getTime());
        timeEntryPO.setDate(timeReport.getDay().getDate());
        TaskContributorPO taskContributorPO = new TaskContributorPO();
        taskContributorPO.setId(timeReport.getTaskContributorId());
        timeEntryPO.setTaskContributor(taskContributorPO);
        updateBaseData(timeEntryPO);
        return timeEntryPO;
    }

    void updateBaseDate(TimeEntryPO timeEntryPO) {
        super.updateBaseData(timeEntryPO);
    }

    boolean isWithinMonth(Day[] days) {
        if (days == null || days.length == 0) {
            return false;
        }

        return days[0].isWithinCurrentMonth() || (days.length > 1 && days[days.length - 1].isWithinCurrentMonth());
    }

    private Set<TaskPO> mergeCategories(List<TimeEntryPO> timeEntryPOS, Set<TaskPO> taskPOS) {
        Set<TaskPO> allCategories = timeEntryPOS.stream().map(TimeEntryPO::getTaskContributor).map(TaskContributorPO::getTask).collect(Collectors.toSet());
        allCategories.addAll(taskPOS);
        return allCategories;
    }

    private Map<Long, TaskContributorPO> createTaskContributorMap(List<TimeEntryPO> timeEntryPOS, List<TaskContributorPO> participations) {
        Map<Long, TaskContributorPO> taskContributorMap = new HashMap<>();
        for (TaskContributorPO taskContributorPO : participations) {
            taskContributorMap.put(taskContributorPO.getTask().getId(), taskContributorPO);
        }

        for (TimeEntryPO timeEntryPO : timeEntryPOS) {
            taskContributorMap.put(timeEntryPO.getTaskContributor().getTask().getId(), timeEntryPO.getTaskContributor());
        }

        return taskContributorMap;
    }

    public List<UserVacation> convertToUserVacations(List<UserPO> userPOs, List<TimeEntryPO> timeEntryPOS, Day[] days) {
        return userPOs.stream()
                .map(u -> convertToUserVacation(u, timeEntryPOS, days))
                .toList();
    }

    private UserVacation convertToUserVacation(UserPO userPO, List<TimeEntryPO> timeEntryPOS, Day[] days) {
        List<TimeEntryPO> timeForUser = timeEntryPOS.stream()
                .filter(t -> t.getTaskContributor().getUser().getId().equals(userPO.getId()))
                .toList();

        VacationDay[] vacationDays = Arrays.stream(days).
                map(d -> VacationDay.builder().day(d).isVacation(isVacation(d, timeForUser)).build()).
                toArray(VacationDay[]::new);

        long noVacationDays = Arrays.stream(vacationDays).filter(v -> v.isVacation()).count();

        return UserVacation.builder().
                userId(userPO.getId()).
                name(userPO.getFullName()).
                noVacationDays((int) noVacationDays).
                vacationsDays(vacationDays).build();
    }

    private boolean isVacation(Day day, List<TimeEntryPO> timeEntryPOS) {
        return timeEntryPOS.stream().anyMatch(t -> t.getDate().equals(day.getDate()));
    }
}
