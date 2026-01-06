package se.dtime.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Task;
import se.dtime.model.TaskType;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.TimeEntryRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskConverter taskConverter;
    @Mock
    private TaskValidator taskValidator;
    @Mock
    private TaskContributorRepository taskContributorRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;

    private Task testTask;
    private TaskPO testTaskPO;
    private Account testAccount;
    private AccountPO testAccountPO;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .name("Test Account")
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        testAccountPO = new AccountPO();
        testAccountPO.setId(1L);
        testAccountPO.setName("Test Account");

        testTask = Task.builder()
                .id(1L)
                .name("Test Task")
                .taskType(TaskType.NORMAL)
                .activationStatus(ActivationStatus.ACTIVE)
                .isBillable(false)
                .account(testAccount)
                .build();

        testTaskPO = new TaskPO();
        testTaskPO.setId(1L);
        testTaskPO.setName("Test Task");
        testTaskPO.setTaskType(TaskType.NORMAL);
        testTaskPO.setActivationStatus(ActivationStatus.ACTIVE);
        testTaskPO.setIsBillable(false);
        testTaskPO.setAccount(testAccountPO);
    }

    @Test
    void add_ValidTask_ShouldReturnSavedTask() {
        // Given
        when(taskConverter.toPO(testTask)).thenReturn(testTaskPO);
        when(taskRepository.save(testTaskPO)).thenReturn(testTaskPO);
        when(taskConverter.toModel(testTaskPO)).thenReturn(testTask);

        // When
        Task result = taskService.add(testTask);

        // Then
        assertThat(result).isEqualTo(testTask);
        verify(taskValidator).validateAdd(testTask);
        verify(taskRepository).save(testTaskPO);
    }

    @Test
    void update_ValidTask_ShouldUpdateTask() {
        // Given
        when(taskConverter.toPO(testTask)).thenReturn(testTaskPO);

        // When
        assertDoesNotThrow(() -> taskService.update(testTask));

        // Then
        verify(taskValidator).validateUpdate(testTask);
        verify(taskRepository).save(testTaskPO);
    }

    @Test
    void update_TaskBecomesInactive_ShouldDeactivateTaskContributors() {
        // Given
        Task inactiveTask = Task.builder()
                .activationStatus(ActivationStatus.INACTIVE)
                .build();

        TaskPO inactiveTaskPO = new TaskPO();
        inactiveTaskPO.setActivationStatus(ActivationStatus.INACTIVE);
        inactiveTaskPO.setIsBillable(false);

        TaskContributorPO taskContributor = new TaskContributorPO();
        taskContributor.setActivationStatus(ActivationStatus.ACTIVE);

        when(taskConverter.toPO(inactiveTask)).thenReturn(inactiveTaskPO);
        when(taskContributorRepository.findByTask(inactiveTaskPO))
                .thenReturn(Collections.singletonList(taskContributor));

        // When
        taskService.update(inactiveTask);

        // Then
        assertThat(taskContributor.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        verify(taskRepository).save(inactiveTaskPO);
    }

    @Test
    void getAll_WithoutFilter_ShouldReturnAllTasks() {
        // Given
        List<TaskPO> taskPOs = Collections.singletonList(testTaskPO);
        Task[] expectedTasks = {testTask};

        when(taskRepository.findAll(any(Sort.class))).thenReturn(taskPOs);
        when(taskConverter.toModel(taskPOs)).thenReturn(expectedTasks);

        // When
        Task[] result = taskService.getAll(null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testTask);
        verify(taskRepository).findAll(any(Sort.class));
    }

    @Test
    void getAll_ActiveTasksOnly_ShouldReturnActiveTasks() {
        // Given
        List<TaskPO> activeTasks = Collections.singletonList(testTaskPO);
        Task[] expectedTasks = {testTask};

        when(taskRepository.findByActivationStatus(ActivationStatus.ACTIVE))
                .thenReturn(activeTasks);
        when(taskConverter.toModel(activeTasks)).thenReturn(expectedTasks);

        // When
        Task[] result = taskService.getAll(true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testTask);
        verify(taskRepository).findByActivationStatus(ActivationStatus.ACTIVE);
    }

    @Test
    void getAll_InactiveTasksOnly_ShouldReturnInactiveTasks() {
        // Given
        TaskPO inactiveTaskPO = new TaskPO();
        inactiveTaskPO.setActivationStatus(ActivationStatus.INACTIVE);
        inactiveTaskPO.setIsBillable(false);

        Task inactiveTask = Task.builder()
                .activationStatus(ActivationStatus.INACTIVE)
                .build();

        List<TaskPO> inactiveTasks = Collections.singletonList(inactiveTaskPO);
        Task[] expectedTasks = {inactiveTask};

        when(taskRepository.findByActivationStatus(ActivationStatus.INACTIVE))
                .thenReturn(inactiveTasks);
        when(taskConverter.toModel(inactiveTasks)).thenReturn(expectedTasks);

        // When
        Task[] result = taskService.getAll(false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(inactiveTask);
        verify(taskRepository).findByActivationStatus(ActivationStatus.INACTIVE);
    }

    @Test
    void get_ValidTaskId_ShouldReturnTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTaskPO));
        when(taskConverter.toModel(testTaskPO)).thenReturn(testTask);

        // When
        Task result = taskService.get(1L);

        // Then
        assertThat(result).isEqualTo(testTask);
        verify(taskRepository).findById(1L);
    }

    @Test
    void get_NonExistentTaskId_ShouldThrowNotFoundException() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> taskService.get(999L));
    }

    @Test
    void delete_ValidTaskId_ShouldDeleteTaskAndRelatedData() {
        // Given
        TaskPO taskPO = new TaskPO(1L);
        when(timeEntryRepository.findByTask(1L)).thenReturn(Collections.emptyList());
        when(taskContributorRepository.findByTask(taskPO)).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> taskService.delete(1L));

        // Then
        verify(taskValidator).validateDelete(1L);
        verify(timeEntryRepository).deleteAll(any());
        verify(taskContributorRepository).deleteAll(any());
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_TaskWithTimeEntries_ShouldDeleteTimeEntriesFirst() {
        // Given
        TaskPO taskPO = new TaskPO(1L);
        List<TimeEntryPO> timeEntries = Arrays.asList(new TimeEntryPO(), new TimeEntryPO());
        List<TaskContributorPO> taskContributors = Arrays.asList(new TaskContributorPO(), new TaskContributorPO());

        when(timeEntryRepository.findByTask(1L)).thenReturn(timeEntries);
        when(taskContributorRepository.findByTask(taskPO)).thenReturn(taskContributors);

        // When
        taskService.delete(1L);

        // Then
        verify(timeEntryRepository).deleteAll(timeEntries);
        verify(taskContributorRepository).deleteAll(taskContributors);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void add_TaskWithVacationType_ShouldPassValidation() {
        // Given
        Task vacationTask = Task.builder()
                .taskType(TaskType.VACATION)
                .build();

        TaskPO vacationTaskPO = new TaskPO();
        vacationTaskPO.setTaskType(TaskType.VACATION);
        vacationTaskPO.setIsBillable(false);

        when(taskConverter.toPO(vacationTask)).thenReturn(vacationTaskPO);
        when(taskRepository.save(vacationTaskPO)).thenReturn(vacationTaskPO);
        when(taskConverter.toModel(vacationTaskPO)).thenReturn(vacationTask);

        // When
        Task result = taskService.add(vacationTask);

        // Then
        assertThat(result.getTaskType()).isEqualTo(TaskType.VACATION);
        verify(taskValidator).validateAdd(vacationTask);
    }

    @Test
    void update_TaskWithSpecialType_ShouldPassValidation() {
        // Given
        Task sickLeaveTask = Task.builder()
                .taskType(TaskType.SICK_LEAVE)
                .build();

        TaskPO sickLeaveTaskPO = new TaskPO();
        sickLeaveTaskPO.setTaskType(TaskType.SICK_LEAVE);
        sickLeaveTaskPO.setIsBillable(false);

        when(taskConverter.toPO(sickLeaveTask)).thenReturn(sickLeaveTaskPO);

        // When
        assertDoesNotThrow(() -> taskService.update(sickLeaveTask));

        // Then
        verify(taskValidator).validateUpdate(sickLeaveTask);
        verify(taskRepository).save(sickLeaveTaskPO);
    }
}