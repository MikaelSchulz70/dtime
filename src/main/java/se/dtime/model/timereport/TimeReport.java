package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.dtime.model.User;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class TimeReport {
    private User user;
    private Day[] days;
    private float toalTime;
    private int workableHours;
    private List<TimeReportProject> timeReportProjectsInternal;
    private List<TimeReportProject> timeReportProjectsExternal;
    private boolean isClosed;

    public LocalDate getFirstDate() {
        return days.length > 0 ? days[0].getDate() : null;
    }

    public LocalDate getLastDate() {
        return days.length > 0 ? days[days.length - 1].getDate() : null;
    }
}
