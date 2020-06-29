package se.dtime.model.timereport;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Day {
    private int year;
    private int month;
    private String monthName;
    private int week;
    private int day;
    private String dayName;
    private boolean isWeekend;
    private boolean isMajorHoliday;
    private LocalDate date;
    private boolean withinCurrentMonth;
}
