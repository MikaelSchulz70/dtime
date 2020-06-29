package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Week {
    private int weekNumber;
    private Day[] days;
}
