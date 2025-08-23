package se.dtime.model.timereport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Day {
    private int year;
    private int month;
    private String monthName;
    private int week;
    private int day;
    private String dayName;
    private boolean isWeekend;
    private boolean isMajorHoliday;
    private boolean isHalfDay;
    private LocalDate date;
    private boolean withinCurrentMonth;

    public boolean isWeekend() {
        return isWeekend;
    }

    public void setWeekend(boolean weekend) {
        isWeekend = weekend;
    }

    public boolean isMajorHoliday() {
        return isMajorHoliday;
    }

    public void setMajorHoliday(boolean majorHoliday) {
        isMajorHoliday = majorHoliday;
    }
}
