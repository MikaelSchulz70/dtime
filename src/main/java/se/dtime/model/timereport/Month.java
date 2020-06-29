package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Month {
    private int month;
    private String monthName;
    private Day[] days;
}
