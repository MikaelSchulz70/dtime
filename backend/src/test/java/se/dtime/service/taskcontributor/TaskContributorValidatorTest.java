package se.dtime.service.taskcontributor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.*;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskContributorValidatorTest {
    @InjectMocks
    private TaskContributorValidator taskContributorValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;

    @BeforeEach
    public void setUp() {
        UserPO userPO = new UserPO(1L);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);
        userPO.setUserRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
    }

    @Test
    public void validateAddOk() {
        TaskPO TaskPO = new TaskPO(1L);
        TaskPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(TaskPO));

        TaskContributor taskContributor = createParticipation();
        taskContributorValidator.validateAdd(taskContributor);
    }

    @Test
    public void validateAddUserNotFound() {
        TaskContributor taskContributor = createParticipation();
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        ValidationException exception = assertThrows(ValidationException.class, () -> taskContributorValidator.validateAdd(taskContributor));
        assertThat(exception.getMessage()).isEqualTo("user.not.found");
    }

    @Test
    public void validateAddUserInactive() {
        TaskContributor taskContributor = createParticipation();

        UserPO userPO = new UserPO(1L);
        userPO.setActivationStatus(ActivationStatus.INACTIVE);
        userPO.setUserRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));

        ValidationException exception = assertThrows(ValidationException.class, () -> taskContributorValidator.validateAdd(taskContributor));
        assertThat(exception.getMessage()).isEqualTo("user.not.active");
    }

    @Test
    public void validateAddTaskNotFound() {
        TaskPO TaskPO = new TaskPO(1L);
        TaskPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(TaskPO));

        TaskContributor taskContributor = createParticipation();
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        ValidationException exception = assertThrows(ValidationException.class, () -> taskContributorValidator.validateAdd(taskContributor));
        assertThat(exception.getMessage()).isEqualTo("task.not.found");
    }

    @Test
    public void validateAddTaskInactive() {
        TaskPO TaskPO = new TaskPO(1L);
        TaskPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(TaskPO));

        TaskContributor taskContributor = createParticipation();

        TaskPO.setActivationStatus(ActivationStatus.INACTIVE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(TaskPO));

        ValidationException exception = assertThrows(ValidationException.class, () -> taskContributorValidator.validateAdd(taskContributor));
        assertThat(exception.getMessage()).isEqualTo("task.not.active");
    }

    private TaskContributor createParticipation() {
        TaskContributor assignment = new TaskContributor();
        assignment.setTask(Task.builder().id(1L).build());
        assignment.setUser(User.builder().id(1L).build());
        return assignment;
    }
}