package se.dtime.service.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.model.timereport.Day;
import se.dtime.model.timereport.Month;
import se.dtime.model.timereport.Week;
import se.dtime.repository.SpecialDayRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CalendarServiceTest {
    private CalendarService calendarService;
    @Mock
    private SpecialDayRepository specialDayRepository;

    @BeforeEach
    void setUp() {
        // Replace "8" with a suitable value for your config
        calendarService = new CalendarService(specialDayRepository, "2020-01-01", 8);
    }

    @Test
    public void testGetMonth() {
        LocalDate date = LocalDate.of(2018, 9, 25);
        Month month = calendarService.getMouth(date);
        Day[] daysInMonth = month.getDays();

        assertEquals(30, daysInMonth.length);
        assertEquals(9, month.getMonth());
        assertEquals("SEPTEMBER", month.getMonthName());

        assertEquals(1, daysInMonth[0].getDay());
        assertTrue(daysInMonth[0].isWeekend());
        assertEquals("SATURDAY", daysInMonth[0].getDayName());
        assertEquals(LocalDate.of(2018, 9, 1), daysInMonth[0].getDate());

        assertEquals(2, daysInMonth[1].getDay());
        assertTrue(daysInMonth[1].isWeekend());
        assertEquals("SUNDAY", daysInMonth[1].getDayName());
        assertEquals(LocalDate.of(2018, 9, 2), daysInMonth[1].getDate());

        assertEquals(3, daysInMonth[2].getDay());
        assertFalse(daysInMonth[2].isWeekend());
        assertEquals("MONDAY", daysInMonth[2].getDayName());
        assertEquals(LocalDate.of(2018, 9, 3), daysInMonth[2].getDate());

        assertEquals(30, daysInMonth[29].getDay());
        assertTrue(daysInMonth[29].isWeekend());
        assertEquals("SUNDAY", daysInMonth[29].getDayName());
        assertEquals(LocalDate.of(2018, 9, 30), daysInMonth[29].getDate());
    }

    @Test
    public void testGetWeek() {
        LocalDate date = LocalDate.of(2018, 9, 25);
        Week week = calendarService.getWeek(date);

        assertEquals(39, week.getWeekNumber());
        assertEquals(7, week.getDays().length);

        assertEquals(24, week.getDays()[0].getDay());
        assertFalse(week.getDays()[0].isWeekend());
        assertEquals("MONDAY", week.getDays()[0].getDayName());
        assertEquals(LocalDate.of(2018, 9, 24), week.getDays()[0].getDate());

        assertEquals(30, week.getDays()[6].getDay());
        assertTrue(week.getDays()[6].isWeekend());
        assertEquals("SUNDAY", week.getDays()[6].getDayName());
        assertEquals(LocalDate.of(2018, 9, 30), week.getDays()[6].getDate());
    }

    @Test
    public void testGetWeekFromYearAndWeekNumber() {
        Week week = calendarService.getWeek(2018, 44);

        assertEquals(44, week.getWeekNumber());
        assertEquals(7, week.getDays().length);

        assertEquals(29, week.getDays()[0].getDay());
        assertFalse(week.getDays()[0].isWeekend());
        assertEquals("MONDAY", week.getDays()[0].getDayName());
        assertEquals(LocalDate.of(2018, 10, 29), week.getDays()[0].getDate());

        assertEquals(4, week.getDays()[6].getDay());
        assertTrue(week.getDays()[6].isWeekend());
        assertEquals("SUNDAY", week.getDays()[6].getDayName());
        assertEquals(LocalDate.of(2018, 11, 4), week.getDays()[6].getDate());
    }

    @Test
    public void testGetNumberOfDaysInMonth() {
        assertEquals(30, calendarService.getNumberOfDaysInMonth(2018, 9));
    }

    @Test
    public void testGetDays() {
        LocalDate fromDate = LocalDate.of(2018, 10, 1);
        LocalDate toDate = LocalDate.of(2018, 9, 20);

        Day[] days = calendarService.getDays(fromDate, toDate);
        assertEquals(0, days.length);

        toDate = LocalDate.of(2018, 10, 31);
        days = calendarService.getDays(fromDate, toDate);
        assertEquals(31, days.length);
    }

    @Test
    public void calcWorkableDaysTest() {
        Day[] days = createDays(LocalDate.of(2018, 12, 1),
                LocalDate.of(2018, 12, 31));

        days[0].setWeekend(true);
        days[1].setWeekend(true);
        days[7].setWeekend(true);
        days[8].setWeekend(true);
        days[14].setWeekend(true);
        days[15].setWeekend(true);
        days[21].setWeekend(true);
        days[22].setWeekend(true);
        days[28].setWeekend(true);
        days[29].setWeekend(true);

        days[23].setMajorHoliday(true);
        days[24].setMajorHoliday(true);
        days[25].setMajorHoliday(true);
        days[30].setMajorHoliday(true);

        assertEquals(136, calendarService.calcWorkableHours(days));
    }

    @Test
    public void getClosestMondayTest() {
        LocalDate mondayDate = LocalDate.of(2018, 12, 10);
        assertEquals(mondayDate, calendarService.getClosestMonday(mondayDate));

        LocalDate date = LocalDate.of(2018, 12, 14);
        assertEquals(mondayDate, calendarService.getClosestMonday(date));

        date = LocalDate.of(2018, 12, 16);
        assertEquals(mondayDate, calendarService.getClosestMonday(date));
    }

    @Test
    public void getLastDayOfMonthTest() {
        assertEquals(LocalDate.of(2018, 1, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 1, 4)));
        assertEquals(LocalDate.of(2018, 2, 28), calendarService.getLastDayOfMonth(LocalDate.of(2018, 2, 3)));
        assertEquals(LocalDate.of(2018, 3, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 3, 3)));
        assertEquals(LocalDate.of(2018, 4, 30), calendarService.getLastDayOfMonth(LocalDate.of(2018, 4, 3)));
        assertEquals(LocalDate.of(2018, 5, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 5, 3)));
        assertEquals(LocalDate.of(2018, 6, 30), calendarService.getLastDayOfMonth(LocalDate.of(2018, 6, 6)));
        assertEquals(LocalDate.of(2018, 7, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 7, 6)));
        assertEquals(LocalDate.of(2018, 8, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 8, 14)));
        assertEquals(LocalDate.of(2018, 9, 30), calendarService.getLastDayOfMonth(LocalDate.of(2018, 9, 14)));
        assertEquals(LocalDate.of(2018, 10, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 10, 14)));
        assertEquals(LocalDate.of(2018, 11, 30), calendarService.getLastDayOfMonth(LocalDate.of(2018, 11, 14)));
        assertEquals(LocalDate.of(2018, 12, 31), calendarService.getLastDayOfMonth(LocalDate.of(2018, 12, 24)));
    }

    @Test
    public void getLastWorkingDayOfMonthTest() {
        assertEquals(LocalDate.of(2018, 1, 31), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 1, 4)));
        assertEquals(LocalDate.of(2018, 2, 28), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 2, 3)));
        assertEquals(LocalDate.of(2018, 3, 30), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 3, 3)));
        assertEquals(LocalDate.of(2018, 4, 30), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 4, 3)));
        assertEquals(LocalDate.of(2018, 5, 31), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 5, 3)));
        assertEquals(LocalDate.of(2018, 6, 29), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 6, 6)));
        assertEquals(LocalDate.of(2018, 7, 31), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 7, 6)));
        assertEquals(LocalDate.of(2018, 8, 31), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 8, 14)));
        assertEquals(LocalDate.of(2018, 9, 28), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 9, 14)));
        assertEquals(LocalDate.of(2018, 10, 31), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 10, 14)));
        assertEquals(LocalDate.of(2018, 11, 30), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 11, 14)));
        assertEquals(LocalDate.of(2018, 12, 31), calendarService.getLastWorkingDayOfMonth(LocalDate.of(2018, 12, 24)));
    }

    private Day[] createDays(LocalDate fromDate, LocalDate toDate) {
        List<Day> days = new ArrayList<>();
        LocalDate date = fromDate;
        while (date.isBefore(toDate) || date.isEqual(toDate)) {
            days.add(Day.builder().date(date).build());
            date = date.plusDays(1);
        }

        return days.toArray(new Day[days.size()]);
    }
}