package se.dtime.service.basis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.model.error.InvalidInputException;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AccountRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyCheckValidatorTest {

    @InjectMocks
    private MonthlyCheckValidator monthlyCheckValidator;

    @Mock
    private AccountRepository accountRepository;

    private MonthlyCheck validMonthlyCheck;
    private AccountPO testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new AccountPO();
        testAccount.setId(1L);
        testAccount.setName("Test Account");

        validMonthlyCheck = MonthlyCheck.builder()
                .accountId(1L)
                .date(LocalDate.of(2024, 1, 1)) // First day of month
                .build();
    }

    @Test
    void validateMonthlyCheck_ValidMonthlyCheck_ShouldPass() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When/Then
        assertDoesNotThrow(() -> monthlyCheckValidator.validateMonthlyCheck(validMonthlyCheck));
    }

    @Test
    void validateMonthlyCheck_AccountNotFound_ShouldThrowNotFoundException() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> monthlyCheckValidator.validateMonthlyCheck(validMonthlyCheck));
        assertThat(exception.getMessage()).isEqualTo("account.not.found");
    }

    @Test
    void validateMonthlyCheck_NullDate_ShouldThrowInvalidInputException() {
        // Given
        MonthlyCheck invalidMonthlyCheck = MonthlyCheck.builder()
                .accountId(1L)
                .date(null)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When/Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> monthlyCheckValidator.validateMonthlyCheck(invalidMonthlyCheck));
        assertThat(exception.getMessage()).isEqualTo("common.invalid.date");
    }

    @Test
    void validateMonthlyCheck_DateNotFirstDayOfMonth_ShouldThrowInvalidInputException() {
        // Given
        MonthlyCheck invalidMonthlyCheck = MonthlyCheck.builder()
                .accountId(1L)
                .date(LocalDate.of(2024, 1, 15)) // 15th day of month, not 1st
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When/Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> monthlyCheckValidator.validateMonthlyCheck(invalidMonthlyCheck));
        assertThat(exception.getMessage()).isEqualTo("common.invalid.date");
    }

    @Test
    void validateMonthlyCheck_DateIsLastDayOfMonth_ShouldThrowInvalidInputException() {
        // Given
        MonthlyCheck invalidMonthlyCheck = MonthlyCheck.builder()
                .accountId(1L)
                .date(LocalDate.of(2024, 1, 31)) // Last day of month
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When/Then
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> monthlyCheckValidator.validateMonthlyCheck(invalidMonthlyCheck));
        assertThat(exception.getMessage()).isEqualTo("common.invalid.date");
    }

    @Test
    void validateMonthlyCheck_DifferentValidMonthDate_ShouldPass() {
        // Given
        MonthlyCheck monthlyCheckDifferentMonth = MonthlyCheck.builder()
                .accountId(1L)
                .date(LocalDate.of(2024, 6, 1)) // First day of June
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When/Then
        assertDoesNotThrow(() -> monthlyCheckValidator.validateMonthlyCheck(monthlyCheckDifferentMonth));
    }

    @Test
    void validateMonthlyCheck_LeapYearFebruaryFirstDay_ShouldPass() {
        // Given
        MonthlyCheck leapYearMonthlyCheck = MonthlyCheck.builder()
                .accountId(1L)
                .date(LocalDate.of(2024, 2, 1)) // First day of February in leap year
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When/Then
        assertDoesNotThrow(() -> monthlyCheckValidator.validateMonthlyCheck(leapYearMonthlyCheck));
    }

    @Test
    void validateMonthlyCheck_NonExistentAccountId_ShouldThrowNotFoundException() {
        // Given
        MonthlyCheck monthlyCheckWithNonExistentAccount = MonthlyCheck.builder()
                .accountId(999L)
                .date(LocalDate.of(2024, 1, 1))
                .build();

        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> monthlyCheckValidator.validateMonthlyCheck(monthlyCheckWithNonExistentAccount));
        assertThat(exception.getMessage()).isEqualTo("account.not.found");
    }
}