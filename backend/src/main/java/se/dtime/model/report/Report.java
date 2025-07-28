package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Report {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final int workableHours;
    private List<UserReport> userReports;
    private List<TaskReport> taskReports;
    private List<TaskUserReport> taskUserReports;
    private int totalWorkableHours;

    public Report(LocalDate fromDate, LocalDate toDate, int workableHours) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.workableHours = workableHours;
    }
}
