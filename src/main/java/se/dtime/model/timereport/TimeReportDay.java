package se.dtime.model.timereport;

import lombok.*;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeReportDay {
    private long id;
    private long idAssignment;
    private Float time;
    @NotNull
    private Day day;
    private boolean isClosed = false;

    public boolean isHasTime() {
        return time != null;
    }
}
