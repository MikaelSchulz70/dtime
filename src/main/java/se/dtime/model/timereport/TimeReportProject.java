package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.dtime.model.Project;

import java.util.List;

@Getter
@Setter
@Builder
public class TimeReportProject {
    private Project project;
    private float totalTime;
    private List<TimeReportDay> timeReportDays;
}
