package se.dtime.model.timereport;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntry {
    private long id;
    private long taskContributorId;
    private BigDecimal time;
    @NotNull
    private Day day;
    private boolean isClosed = false;

    public boolean isHasTime() {
        return time != null;
    }
}
