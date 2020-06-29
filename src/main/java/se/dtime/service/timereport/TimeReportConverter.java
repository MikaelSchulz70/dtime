package se.dtime.service.timereport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.TimeReportDayPO;
import se.dtime.model.timereport.*;
import se.dtime.service.BaseConverter;
import se.dtime.service.project.ProjectConverter;
import se.dtime.service.user.UserConverter;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeReportConverter extends BaseConverter {

    @Autowired
    private ProjectConverter projectConverter;
    @Autowired
    private UserConverter userConverter;

    TimeReport convertToTimeReport(Day[] days, UserPO userPO, List<TimeReportDayPO> timeReportDayPOS, List<AssignmentPO> assignments) {
        TimeReport timeReport = TimeReport.builder().
                days(days).
                user(userConverter.toModel(userPO)).
                build();

        List<TimeReportDayPO> internalTimeReportDays = timeReportDayPOS.stream().filter(d -> d.getAssignment().getProject().isInternal()).collect(Collectors.toList());
        List<TimeReportDayPO> externalTimeReportDays = timeReportDayPOS.stream().filter(d -> !d.getAssignment().getProject().isInternal()).collect(Collectors.toList());

        Set<ProjectPO> internalProjects = assignments.stream().map(AssignmentPO::getProject).filter(ProjectPO::isInternal).collect(Collectors.toSet());
        Set<ProjectPO> externalProjects = assignments.stream().map(AssignmentPO::getProject).filter(p -> !p.isInternal()).collect(Collectors.toSet());
        Map<Long, AssignmentPO> projectAssignmentMap = createProjectAssignmentMap(timeReportDayPOS, assignments);

        timeReport.setTimeReportProjectsInternal(convertToTimeReportProject(days, projectAssignmentMap, internalTimeReportDays, internalProjects));
        timeReport.setTimeReportProjectsExternal(convertToTimeReportProject(days, projectAssignmentMap, externalTimeReportDays, externalProjects));

        double totalTime = timeReport.getTimeReportProjectsExternal().stream().mapToDouble(TimeReportProject::getTotalTime).sum();
        totalTime += timeReport.getTimeReportProjectsInternal().stream().mapToDouble(TimeReportProject::getTotalTime).sum();
        timeReport.setToalTime((float) totalTime);

        return timeReport;
    }

    private List<TimeReportProject> convertToTimeReportProject(Day[] days, Map<Long, AssignmentPO> projectAssignmentMap, List<TimeReportDayPO> timeReportDayPOS, Set<ProjectPO> projectPOS) {
        List<TimeReportProject> timeReportProjects = new ArrayList<>();

        Set<ProjectPO> allProjects = mergeProjects(timeReportDayPOS, projectPOS);

        for (ProjectPO projectPO : allProjects) {
            TimeReportProject timeReportProject = TimeReportProject.builder().
                    project(projectConverter.toModel(projectPO)).
                    timeReportDays(new ArrayList<>()).
                    totalTime(0.0f).
                    build();

            timeReportProjects.add(timeReportProject);

            for (Day day : days) {
                TimeReportDay timeReportDay = timeReportDayPOS.stream().
                        filter(t -> t.getAssignment().getProject().getId().equals(projectPO.getId()) && day.getDate().equals(t.getDate())).
                        findFirst().map(trd -> convertToTimeReportDay(day, trd)).
                        orElse(convertToTimeReportDay(day, projectAssignmentMap.get(projectPO.getId())));

                timeReportProject.setTotalTime(timeReportProject.getTotalTime() + (timeReportDay.getTime() != null ? timeReportDay.getTime() : 0f));
                timeReportProject.getTimeReportDays().add(timeReportDay);
            }
        }

        timeReportProjects.sort((TimeReportProject t1, TimeReportProject t2) -> t1.getProject().getName().compareTo(t2.getProject().getName()));

        return timeReportProjects;
    }

    TimeReportDay convertToTimeReportDay(Day day, AssignmentPO assignmentPO) {
        return TimeReportDay.builder().
                id(0).
                idAssignment(assignmentPO.getId()).
                time(null).
                day(day).
                build();
    }

    TimeReportDay convertToTimeReportDay(Day day, TimeReportDayPO timeReportDayPO) {
        return TimeReportDay.builder().
                id(timeReportDayPO.getId()).
                idAssignment(timeReportDayPO.getAssignment().getId()).
                time(timeReportDayPO.getTime()).
                day(day).
                build();

    }

    public TimeReportDayPO toPO(TimeReportDay timeReport) {
        TimeReportDayPO timeReportDayPO = new TimeReportDayPO();
        timeReportDayPO.setTime(timeReport.getTime() == null ? 0f : timeReport.getTime());
        timeReportDayPO.setDate(timeReport.getDay().getDate());
        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setId(timeReport.getIdAssignment());
        timeReportDayPO.setAssignment(assignmentPO);
        updateBaseData(timeReportDayPO);
        return timeReportDayPO;
    }

    void updateBaseDate(TimeReportDayPO timeReportDayPO) {
        super.updateBaseData(timeReportDayPO);
    }

    boolean isWithinMonth(Day[] days) {
        if (days == null || days.length == 0) {
            return false;
        }

        return days[0].isWithinCurrentMonth() || (days.length > 1 && days[days.length - 1].isWithinCurrentMonth());
    }

    private Set<ProjectPO> mergeProjects(List<TimeReportDayPO> timeReportDayPOS, Set<ProjectPO> projectPOS) {
        Set<ProjectPO> allProjects = timeReportDayPOS.stream().map(TimeReportDayPO::getAssignment).map(AssignmentPO::getProject).collect(Collectors.toSet());
        allProjects.addAll(projectPOS);
        return allProjects;
    }

    private Map<Long, AssignmentPO> createProjectAssignmentMap(List<TimeReportDayPO> timeReportDayPOS, List<AssignmentPO> assignments) {
        Map<Long, AssignmentPO> projectAssignmentMap = new HashMap<>();
        for (AssignmentPO assignmentPO : assignments) {
            projectAssignmentMap.put(assignmentPO.getProject().getId(), assignmentPO);
        }

        for (TimeReportDayPO timeReportDayPO : timeReportDayPOS) {
            projectAssignmentMap.put(timeReportDayPO.getAssignment().getProject().getId(), timeReportDayPO.getAssignment());
        }

        return projectAssignmentMap;
    }

    public List<UserVacation> convertToUserVacations(List<UserPO> userPOs, List<TimeReportDayPO> timeReportDayPOS, Day[] days) {
        return userPOs.stream().map(u -> convertToUserVacation(u, timeReportDayPOS, days)).collect(Collectors.toList());
    }

    private UserVacation convertToUserVacation(UserPO userPO, List<TimeReportDayPO> timeReportDayPOS, Day[] days) {
        List<TimeReportDayPO> timeForUser = timeReportDayPOS.stream().filter(t -> t.getAssignment().getUser().getId().equals(userPO.getId())).collect(Collectors.toList());

        VacationDay[] vacationDays = Arrays.stream(days).
                map(d -> VacationDay.builder().day(d).isVacation(isVacation(d, timeForUser)).build()).
                toArray(VacationDay[]::new);

        long noVacationDays = Arrays.stream(vacationDays).filter(v -> v.isVacation()).count();

        return UserVacation.builder().
                idUser(userPO.getId()).
                name(userPO.getFullName()).
                noVacationDays((int) noVacationDays).
                vacationsDays(vacationDays).build();
    }

    private boolean isVacation(Day day, List<TimeReportDayPO> timeReportDayPOS) {
        return timeReportDayPOS.stream().anyMatch(t -> t.getDate().equals(day.getDate()));
    }
}
