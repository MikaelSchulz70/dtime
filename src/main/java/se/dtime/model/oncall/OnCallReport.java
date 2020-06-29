package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.dtime.model.timereport.Day;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class OnCallReport {
    private Day[] days;
    private List<OnCallProject> onCallProjects;
    private boolean isReadOnly;

    public LocalDate getFirstDate() {
        return days.length > 0 ? days[0].getDate() : null;
    }

    public LocalDate getLastDate() {
        return days.length > 0 ? days[days.length - 1].getDate() : null;
    }
}
