package se.dtime.service.period;

import org.springframework.stereotype.Component;
import se.dtime.model.error.InvalidInputException;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@Component
public class PeriodValidator {

    private final CalendarService calendarService;

    public PeriodValidator(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public void validateYear(int year) {
        int minYear = calendarService.getSystemStartDate().getYear();
        if (year < minYear) {
            throw new InvalidInputException("invalid.year.input");
        }
    }

    public void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new InvalidInputException("invalid.month.input");
        }
    }

    public void validateWeek(int week) {
        if (week < 1 || week > 53) {
            throw new InvalidInputException("invalid.week.input");
        }
    }

    public void validateWeekOfYear(int year, int week) {
        validateYear(year);
        validateWeek(week);
        int maxWeek = (int) WeekFields.ISO.weekOfWeekBasedYear().rangeRefinedBy(
                LocalDate.of(year, 12, 28)).getMaximum();
        if (week > maxWeek) {
            throw new InvalidInputException("invalid.week.input");
        }
    }
}
