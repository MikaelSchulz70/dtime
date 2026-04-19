package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.dtime.model.Task;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class TimeReportTask {
    private Task task;
    private BigDecimal totalTime;
    private List<TimeEntry> timeEntries;
}
