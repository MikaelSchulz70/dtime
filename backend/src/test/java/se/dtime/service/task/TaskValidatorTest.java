package se.dtime.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.Account;
import se.dtime.model.Task;
import se.dtime.model.TaskType;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.TimeEntryRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskValidatorTest {
    @InjectMocks
    private TaskValidator taskValidator;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskContributorRepository taskContributorRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;

    @BeforeEach
    public void setup() {
        taskValidator.init();
    }

    @Test
    public void validateTaskType_NormalTask_ShouldPass() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.NORMAL)
                .account(account)
                .build();

        // When/Then - Should not throw any exception
        assertDoesNotThrow(() -> taskValidator.validateAdd(task));
    }

    @Test
    public void validateTaskType_VacationTask_NoExisting_ShouldPass() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.VACATION)
                .account(account)
                .build();

        when(taskRepository.findByTaskTypeAndAccount(eq(TaskType.VACATION), any(AccountPO.class)))
                .thenReturn(Collections.emptyList());

        // When/Then - Should not throw any exception
        assertDoesNotThrow(() -> taskValidator.validateAdd(task));
    }

    @Test
    public void validateTaskType_VacationTask_ExistingVacationTask_ShouldFail() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.VACATION)
                .account(account)
                .build();

        TaskPO existingVacationTask = new TaskPO(2L);
        when(taskRepository.findByTaskTypeAndAccount(eq(TaskType.VACATION), any(AccountPO.class)))
                .thenReturn(List.of(existingVacationTask));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> taskValidator.validateAdd(task));
        assertThat(exception.getMessage()).isEqualTo("task.vacation.already.exists");
    }

    @Test
    public void validateTaskType_SickLeaveTask_ExistingSickLeaveTask_ShouldFail() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.SICK_LEAVE)
                .account(account)
                .build();

        TaskPO existingSickLeaveTask = new TaskPO(2L);
        when(taskRepository.findByTaskTypeAndAccount(eq(TaskType.SICK_LEAVE), any(AccountPO.class)))
                .thenReturn(List.of(existingSickLeaveTask));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> taskValidator.validateAdd(task));
        assertThat(exception.getMessage()).isEqualTo("task.sick.leave.already.exists");
    }

    @Test
    public void validateTaskType_ParentalLeaveTask_ExistingParentalLeaveTask_ShouldFail() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.PARENTAL_LEAVE)
                .account(account)
                .build();

        TaskPO existingParentalLeaveTask = new TaskPO(2L);
        when(taskRepository.findByTaskTypeAndAccount(eq(TaskType.PARENTAL_LEAVE), any(AccountPO.class)))
                .thenReturn(List.of(existingParentalLeaveTask));

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, 
                () -> taskValidator.validateAdd(task));
        assertThat(exception.getMessage()).isEqualTo("task.parental.leave.already.exists");
    }

    @Test
    public void validateTaskType_UpdateSameTask_ShouldPass() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.VACATION)
                .account(account)
                .build();

        // Existing task is the same task being updated
        TaskPO existingVacationTask = new TaskPO(1L);
        when(taskRepository.findByTaskTypeAndAccount(eq(TaskType.VACATION), any(AccountPO.class)))
                .thenReturn(List.of(existingVacationTask));

        // When/Then - Should not throw any exception since it's the same task
        assertDoesNotThrow(() -> taskValidator.validateAdd(task));
    }

    @Test
    public void validateTaskType_MultipleNormalTasks_ShouldPass() {
        // Given
        Account account = Account.builder().id(1L).build();
        Task task = Task.builder()
                .id(1L)
                .taskType(TaskType.NORMAL)
                .account(account)
                .build();

        // When/Then - Should not throw any exception for NORMAL tasks
        // No repository mocking needed since NORMAL tasks skip validation
        assertDoesNotThrow(() -> taskValidator.validateAdd(task));
    }
}