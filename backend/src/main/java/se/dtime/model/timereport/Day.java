package se.dtime.model.timereport;

import lombok.*;

import java.time.LocalDate;

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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isWithinCurrentMonth() {
        return withinCurrentMonth;
    }

    public void setWithinCurrentMonth(boolean withinCurrentMonth) {
        this.withinCurrentMonth = withinCurrentMonth;
    }
}
