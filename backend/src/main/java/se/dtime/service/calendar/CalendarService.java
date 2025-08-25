package se.dtime.service.calendar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.timereport.Day;
import se.dtime.model.timereport.DayType;
import se.dtime.model.timereport.Month;
import se.dtime.model.timereport.Week;
import se.dtime.repository.SpecialDayRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

@Slf4j
@Service
public class CalendarService {
    private final static int DAYS_OF_WEEK = 7;

    private final SpecialDayRepository specialDayRepository;
    private final float hoursPerDay;
    private final LocalDate systemStartDate;
    private static final Map<Integer, Map<LocalDate, DayType>> specialDaysByYear = new HashMap<>();

    public CalendarService(SpecialDayRepository specialDayRepository,
                           @Value("${dtime.system.start-date}") String systemStartDate,
                           @Value("${dtime.hours_per_day}") float hoursPerDay) {
        this.specialDayRepository = specialDayRepository;
        this.systemStartDate = LocalDate.parse(systemStartDate);
        this.hoursPerDay = hoursPerDay;
    }

    public LocalDate getNowDate() {
        return LocalDate.now();
    }

    public LocalDateTime getNowDateTime() {
        return LocalDateTime.now();
    }

    public Day getCurrentDay() {
        return getDay(getNowDate());
    }

    public Month getMouth(LocalDate date) {
        Day[] daysInMouth = new Day[date.lengthOfMonth()];
        LocalDate day = date.withDayOfMonth(1);

        for (int d = 0; d < date.lengthOfMonth(); d++) {
            Day nextDay = getDay(day);
            daysInMouth[d] = nextDay;
            day = day.plusDays(1);
        }

        return Month.builder().month(date.getMonthValue()).monthName(date.getMonth().name()).days(daysInMouth).build();
    }

    public Week getWeek(int year, int week) {
        LocalDate date = LocalDate.of(year, 1, 1);
        LocalDate fromDate = date.with(
                TemporalAdjusters.dayOfWeekInMonth(week, DayOfWeek.MONDAY));
        return getWeek(fromDate);
    }

    public Week getWeek(LocalDate date) {
        Day[] daysInWeek = new Day[DAYS_OF_WEEK];

        LocalDate day = getClosestMonday(date);
        LocalDate firstDay = day;
        for (int d = 0; d < DAYS_OF_WEEK; d++) {
            Day nextDay = getDay(day);
            daysInWeek[d] = nextDay;
            day = day.plusDays(1);
        }

        TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        return Week.builder().weekNumber(firstDay.get(woy)).days(daysInWeek).build();
    }

    public LocalDate getClosestMonday(LocalDate date) {
        LocalDate closestMondayDate = date;
        while (closestMondayDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            closestMondayDate = closestMondayDate.minusDays(1);
        }

        return closestMondayDate;
    }

    Day getDay(LocalDate date) {
        // Get special days for this year from repository
        Map<LocalDate, DayType> specialDaysForYear = getSpecialDaysForYear(date.getYear());

        // Check if this date is a special day
        DayType specialDayType = specialDaysForYear.get(date);
        boolean isMajorHoliday = specialDayType == DayType.PUBLIC_HOLIDAY;
        boolean isHalfDay = specialDayType == DayType.HALF_DAY;

        return Day.builder().
                year(date.getYear()).
                month(date.getMonthValue()).
                monthName(date.getMonth().name()).
                day(date.getDayOfMonth()).
                date(date).
                isWeekend(isWeekend(date)).
                isMajorHoliday(isMajorHoliday).
                isHalfDay(isHalfDay).
                dayName(date.getDayOfWeek().name()).
                withinCurrentMonth(isWithinCurrentMonth(date)).
                build();
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private Map<LocalDate, DayType> getSpecialDaysForYear(int year) {
        Map<LocalDate, DayType> specialDaysForYear = specialDaysByYear.get(year);
        if (specialDaysForYear == null) {
            // Fetch special days for this year from repository
            List<SpecialDayPO> specialDayPOs = specialDayRepository.findByYear(year);

            // Convert to map for efficient lookup
            specialDaysForYear = new HashMap<>();
            for (SpecialDayPO specialDayPO : specialDayPOs) {
                specialDaysForYear.put(specialDayPO.getDate(), specialDayPO.getDayType());
            }

            // Cache for future use
            specialDaysByYear.put(year, specialDaysForYear);
        }

        return specialDaysForYear;
    }

    public int getNumberOfDaysInMonth(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        return date.lengthOfMonth();
    }

    public Day[] getDays(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            return new Day[0];
        }

        List<Day> days = new ArrayList<>();
        LocalDate date = fromDate;
        while (date.isBefore(toDate) || date.isEqual(toDate)) {
            days.add(getDay(date));
            date = date.plusDays(1);
        }

        return days.toArray(new Day[0]);
    }

    public int calcWorkableHours(Day[] days) {
        int totalHours = 0;
        for (Day day : days) {
            if (!day.isWeekend() && !day.isMajorHoliday()) {
                if (day.isHalfDay()) {
                    totalHours += hoursPerDay / 2;
                } else {
                    totalHours += hoursPerDay;
                }
            }
        }
        return totalHours;
    }

    public boolean isWithinCurrentMonth(LocalDate date) {
        LocalDate now = getNowDate();
        return now.getMonthValue() == date.getMonthValue();
    }

    public LocalDate getLastDayOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    public LocalDate getLastWorkingDayOfMonth(LocalDate date) {
        LocalDate lastDayOfMonth = getLastDayOfMonth(date);
        LocalDate lastWorkingDayOfMonth;
        switch (DayOfWeek.of(lastDayOfMonth.get(ChronoField.DAY_OF_WEEK))) {
            case SATURDAY:
                lastWorkingDayOfMonth = lastDayOfMonth.minusDays(1);
                break;
            case SUNDAY:
                lastWorkingDayOfMonth = lastDayOfMonth.minusDays(2);
                break;
            default:
                lastWorkingDayOfMonth = lastDayOfMonth;
        }

        return lastWorkingDayOfMonth;
    }

    public LocalDate getSystemStartDate() {
        return systemStartDate;
    }

    public void resetSpecialDaysCache() {
        specialDaysByYear.clear();
    }
}
