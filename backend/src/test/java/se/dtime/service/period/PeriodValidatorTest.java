package se.dtime.service.period;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.dtime.model.error.InvalidInputException;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PeriodValidatorTest {

    @Mock
    private CalendarService calendarService;

    private PeriodValidator periodValidator;

    @BeforeEach
    void setUp() {
        when(calendarService.getSystemStartDate()).thenReturn(LocalDate.of(2020, 1, 1));
        periodValidator = new PeriodValidator(calendarService);
    }

    @Test
    void validateYear_acceptsFromSystemStart() {
        assertDoesNotThrow(() -> periodValidator.validateYear(2020));
    }

    @Test
    void validateYear_rejectsBeforeSystemStart() {
        assertThrows(InvalidInputException.class, () -> periodValidator.validateYear(2019));
    }

    @Test
    void validateMonth_acceptsValidRange() {
        assertDoesNotThrow(() -> periodValidator.validateMonth(1));
        assertDoesNotThrow(() -> periodValidator.validateMonth(12));
    }

    @Test
    void validateMonth_rejectsOutOfRange() {
        assertThrows(InvalidInputException.class, () -> periodValidator.validateMonth(0));
        assertThrows(InvalidInputException.class, () -> periodValidator.validateMonth(13));
    }

    @Test
    void validateWeek_acceptsValidRange() {
        assertDoesNotThrow(() -> periodValidator.validateWeek(1));
        assertDoesNotThrow(() -> periodValidator.validateWeek(53));
    }

    @Test
    void validateWeek_rejectsOutOfRange() {
        assertThrows(InvalidInputException.class, () -> periodValidator.validateWeek(0));
        assertThrows(InvalidInputException.class, () -> periodValidator.validateWeek(54));
    }

    @Test
    void validateWeekOfYear_rejectsWeek53WhenYearHas52Weeks() {
        assertThrows(InvalidInputException.class, () -> periodValidator.validateWeekOfYear(2021, 53));
    }

    @Test
    void validateWeekOfYear_acceptsWeek53WhenYearHas53Weeks() {
        assertDoesNotThrow(() -> periodValidator.validateWeekOfYear(2020, 53));
    }
}
