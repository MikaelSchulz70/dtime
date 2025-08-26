package se.dtime.service.specialday;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.SpecialDay;
import se.dtime.model.error.ValidationException;
import se.dtime.model.timereport.DayType;
import se.dtime.repository.SpecialDayRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialDayValidatorTest {

    @InjectMocks
    private SpecialDayValidator specialDayValidator;

    @Mock
    private SpecialDayRepository specialDayRepository;

    private SpecialDay validSpecialDay;

    @BeforeEach
    void setUp() {
        validSpecialDay = SpecialDay.builder()
                .id(1L)
                .name("New Year's Day")
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    void validateCreate_ValidSpecialDay_ShouldPass() {
        // Given
        when(specialDayRepository.existsByDate(validSpecialDay.getDate())).thenReturn(false);

        // When/Then
        assertDoesNotThrow(() -> specialDayValidator.validateCreate(validSpecialDay));
    }

    @Test
    void validateCreate_NullSpecialDay_ShouldThrowException() {
        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(null));
        assertThat(exception.getMessage()).isEqualTo("special.day.required");
    }

    @Test
    void validateCreate_NullName_ShouldThrowException() {
        // Given
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .name(null)
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.name.required");
    }

    @Test
    void validateCreate_EmptyName_ShouldThrowException() {
        // Given
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .name("   ")
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.name.required");
    }

    @Test
    void validateCreate_NameTooLong_ShouldThrowException() {
        // Given
        String longName = "A".repeat(41);
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .name(longName)
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.name.too.long");
    }

    @Test
    void validateCreate_NullDayType_ShouldThrowException() {
        // Given
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .name("Valid Name")
                .dayType(null)
                .date(LocalDate.now().plusDays(30))
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.type.required");
    }

    @Test
    void validateCreate_NullDate_ShouldThrowException() {
        // Given
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .name("Valid Name")
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(null)
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.date.required");
    }

    @Test
    void validateCreate_DateTooOld_ShouldThrowException() {
        // Given
        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .name("Valid Name")
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(threeYearsAgo)
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.date.too.old");
    }

    @Test
    void validateCreate_DateAlreadyExists_ShouldThrowException() {
        // Given
        when(specialDayRepository.existsByDate(validSpecialDay.getDate())).thenReturn(true);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateCreate(validSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.date.already.exists");
    }

    @Test
    void validateUpdate_ValidSpecialDay_ShouldPass() {
        // Given
        when(specialDayRepository.existsById(1L)).thenReturn(true);
        when(specialDayRepository.findByDate(validSpecialDay.getDate()))
                .thenReturn(Optional.empty());

        // When/Then
        assertDoesNotThrow(() -> specialDayValidator.validateUpdate(validSpecialDay));
    }

    @Test
    void validateUpdate_NullId_ShouldThrowException() {
        // Given
        SpecialDay invalidSpecialDay = SpecialDay.builder()
                .id(null)
                .name("Valid Name")
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(LocalDate.now().plusDays(30))
                .build();

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateUpdate(invalidSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.id.required.for.update");
    }

    @Test
    void validateUpdate_NonExistentId_ShouldThrowException() {
        // Given
        when(specialDayRepository.existsById(1L)).thenReturn(false);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateUpdate(validSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.not.found");
    }

    @Test
    void validateUpdate_DateExistsForDifferentSpecialDay_ShouldThrowException() {
        // Given
        SpecialDayPO existingSpecialDay = new SpecialDayPO();
        existingSpecialDay.setId(2L); // Different ID

        when(specialDayRepository.existsById(1L)).thenReturn(true);
        when(specialDayRepository.findByDate(validSpecialDay.getDate()))
                .thenReturn(Optional.of(existingSpecialDay));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> specialDayValidator.validateUpdate(validSpecialDay));
        assertThat(exception.getMessage()).isEqualTo("special.day.date.already.exists");
    }

    @Test
    void validateUpdate_DateExistsForSameSpecialDay_ShouldPass() {
        // Given
        SpecialDayPO existingSpecialDay = new SpecialDayPO();
        existingSpecialDay.setId(1L); // Same ID

        when(specialDayRepository.existsById(1L)).thenReturn(true);
        when(specialDayRepository.findByDate(validSpecialDay.getDate()))
                .thenReturn(Optional.of(existingSpecialDay));

        // When/Then
        assertDoesNotThrow(() -> specialDayValidator.validateUpdate(validSpecialDay));
    }

    @Test
    void validateCreate_MaxLengthName_ShouldPass() {
        // Given
        String maxLengthName = "A".repeat(40); // Exactly 40 characters
        SpecialDay specialDayWithMaxName = SpecialDay.builder()
                .name(maxLengthName)
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(LocalDate.now().plusDays(30))
                .build();

        when(specialDayRepository.existsByDate(specialDayWithMaxName.getDate())).thenReturn(false);

        // When/Then
        assertDoesNotThrow(() -> specialDayValidator.validateCreate(specialDayWithMaxName));
    }

    @Test
    void validateCreate_DateExactlyTwoYearsAgo_ShouldPass() {
        // Given
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        SpecialDay specialDayTwoYearsAgo = SpecialDay.builder()
                .name("Valid Name")
                .dayType(DayType.PUBLIC_HOLIDAY)
                .date(twoYearsAgo)
                .build();

        when(specialDayRepository.existsByDate(twoYearsAgo)).thenReturn(false);

        // When/Then
        assertDoesNotThrow(() -> specialDayValidator.validateCreate(specialDayTwoYearsAgo));
    }

    @Test
    void validateCreate_HalfDayType_ShouldPass() {
        // Given
        SpecialDay halfDaySpecialDay = SpecialDay.builder()
                .name("Valid Name")
                .dayType(DayType.HALF_DAY)
                .date(LocalDate.now().plusDays(30))
                .build();

        when(specialDayRepository.existsByDate(halfDaySpecialDay.getDate())).thenReturn(false);

        // When/Then
        assertDoesNotThrow(() -> specialDayValidator.validateCreate(halfDaySpecialDay));
    }
}