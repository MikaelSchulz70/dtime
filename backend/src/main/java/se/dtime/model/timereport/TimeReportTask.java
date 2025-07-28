package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.dtime.model.Task;

import java.util.List;

@Getter
@Setter
@Builder
public class TimeReportTask {
    private Task task;
    private float totalTime;
    private List<TimeEntry> timeEntries;
}
