package se.dtime.service.calendar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.model.timereport.Day;
import se.dtime.model.timereport.Month;
import se.dtime.model.timereport.Week;
import se.dtime.service.system.SystemProperty;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class CalendarService {
    private final static int DAYS_OF_WEEK = 7;
    private final int WORKABLE_HOURS_PER_DAY = 8;

    @Autowired
    private Environment environment;
    @Autowired
    private PublicHolidaysGenerator publicHolidaysGenerator;

    private static LocalDate systemStartDate;
    private static Map<Integer, Set<LocalDate>> majorHolidays = new HashMap<>();


    @PostConstruct
    public void setUp() {
        if (systemStartDate == null) {
            String startDate = environment.getProperty(SystemProperty.SYSTEM_START_DATE_PROP);
            if (StringUtils.isEmpty(startDate)) {
                log.warn("System start date not configured");
            }

            systemStartDate = LocalDate.parse(startDate);
        }
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

        Month month = Month.builder().month(date.getMonthValue()).monthName(date.getMonth().name()).days(daysInMouth).build();

        return month;
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
        return Day.builder().
                year(date.getYear()).
                month(date.getMonthValue()).
                monthName(date.getMonth().name()).
                day(date.getDayOfMonth()).
                date(date).
                isWeekend(isWeekend(date)).
                isMajorHoliday(isMajorHoliday(date)).
                dayName(date.getDayOfWeek().name()).
                withinCurrentMonth(isWithinCurrentMonth(date)).
                build();
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public boolean isMajorHoliday(LocalDate date) {
        Set<LocalDate> majorHolidaysForYear = majorHolidays.get(date.getYear());
        if (majorHolidaysForYear == null) {
            majorHolidaysForYear = publicHolidaysGenerator.generate(date.getYear());
            majorHolidays.put(date.getYear(), majorHolidaysForYear);
        }

        return majorHolidaysForYear.contains(date);
    }

    public int getNumberOfDaysInMonth(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        return date.lengthOfMonth();
    }

    public Day[] getDays(LocalDate fromDate, LocalDate toDate) {
        if(fromDate.isAfter(toDate)) {
            return new Day[0];
        }

        List<Day> days = new ArrayList<>();
        LocalDate date = fromDate;
        while (date.isBefore(toDate) || date.isEqual(toDate)) {
            days.add(getDay(date));
            date = date.plusDays(1);
        }

        return days.toArray(new Day[days.size()]);
    }

    public int calcWorkableHours(Day[] days) {
        return (int) Stream.of(days).filter(d -> !d.isWeekend() && !d.isMajorHoliday()).count() * WORKABLE_HOURS_PER_DAY;
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
}
