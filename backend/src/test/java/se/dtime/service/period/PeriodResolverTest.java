package se.dtime.service.period;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.model.ReportDates;
import se.dtime.model.error.InvalidInputException;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodResolverTest {

    @Mock
    private CalendarService calendarService;

    private PeriodResolver periodResolver;

    @BeforeEach
    void setUp() {
        when(calendarService.getSystemStartDate()).thenReturn(LocalDate.of(2020, 1, 1));
        periodResolver = new PeriodResolver(new PeriodValidator(calendarService));
    }

    @Test
    void resolveMonth_returnsFullMonth() {
        ReportDates dates = periodResolver.resolveMonth(2026, 7);
        assertEquals(LocalDate.of(2026, 7, 1), dates.getFromDate());
        assertEquals(LocalDate.of(2026, 7, 31), dates.getToDate());
    }

    @Test
    void resolveMonth_rejectsInvalidMonth() {
        assertThrows(InvalidInputException.class, () -> periodResolver.resolveMonth(2026, 13));
    }

    @Test
    void resolveYear_returnsFullYear() {
        ReportDates dates = periodResolver.resolveYear(2026);
        assertEquals(LocalDate.of(2026, 1, 1), dates.getFromDate());
        assertEquals(LocalDate.of(2026, 12, 31), dates.getToDate());
    }

    @Test
    void resolveIsoWeek_returnsMondayToSunday() {
        ReportDates dates = periodResolver.resolveIsoWeek(2026, 36);
        assertEquals(LocalDate.of(2026, 8, 31), dates.getFromDate());
        assertEquals(LocalDate.of(2026, 9, 6), dates.getToDate());
    }

    @Test
    void resolveIsoWeek_week53Of2020() {
        ReportDates dates = periodResolver.resolveIsoWeek(2020, 53);
        assertEquals(LocalDate.of(2020, 12, 28), dates.getFromDate());
        assertEquals(LocalDate.of(2021, 1, 3), dates.getToDate());
    }
}
