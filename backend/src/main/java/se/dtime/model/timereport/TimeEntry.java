package se.dtime.model.timereport;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntry {
    private long id;
    private long taskContributorId;
    private Float time;
    @NotNull
    private Day day;
    private boolean isClosed = false;

    public boolean isHasTime() {
        return time != null;
    }
}
