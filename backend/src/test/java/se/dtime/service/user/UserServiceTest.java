package se.dtime.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.User;
import se.dtime.model.UserRole;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.UserRepository;

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
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserConverter userConverter;
    @Mock
    private UserValidator userValidator;
    @Mock
    private TaskContributorRepository taskContributorRepository;

    private User testUser;
    private UserPO testUserPO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .activationStatus(ActivationStatus.ACTIVE)
                .userRole(UserRole.USER)
                .build();

        testUserPO = new UserPO();
        testUserPO.setId(1L);
        testUserPO.setFirstName("John");
        testUserPO.setLastName("Doe");
        testUserPO.setEmail("john.doe@example.com");
        testUserPO.setActivationStatus(ActivationStatus.ACTIVE);
        testUserPO.setUserRole(UserRole.USER);
        testUserPO.setExternalId("external-john-doe");
    }

    @Test
    void deactivate_ActiveUser_ShouldSetInactiveAndDeactivateTaskContributors() {
        TaskContributorPO taskContributor = new TaskContributorPO();
        taskContributor.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserPO));
        when(taskContributorRepository.findByUser(testUserPO))
                .thenReturn(Collections.singletonList(taskContributor));

        assertDoesNotThrow(() -> userService.deactivate(1L));

        verify(userValidator).validateDeactivate(1L);
        assertThat(testUserPO.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        assertThat(taskContributor.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        verify(userRepository).save(testUserPO);
    }

    @Test
    void deactivate_UserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deactivate(1L));
        verify(userValidator).validateDeactivate(1L);
    }

    @Test
    void activate_InactiveUser_ShouldSetActive() {
        testUserPO.setActivationStatus(ActivationStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserPO));

        assertDoesNotThrow(() -> userService.activate(1L));

        verify(userValidator).validateActivate(1L);
        assertThat(testUserPO.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
        verify(userRepository).save(testUserPO);
    }

    @Test
    void getAll_WithoutFilter_ShouldReturnAllUsers() {
        List<UserPO> allUsers = Collections.singletonList(testUserPO);
        User[] expectedUsers = {testUser};

        when(userRepository.findAll(any(Sort.class))).thenReturn(allUsers);
        when(userConverter.toModel(allUsers)).thenReturn(expectedUsers);

        User[] result = userService.getAll(null);

        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testUser);
        verify(userRepository).findAll(any(Sort.class));
    }

    @Test
    void getAll_ActiveUsersOnly_ShouldReturnActiveUsers() {
        List<UserPO> activeUsers = Collections.singletonList(testUserPO);
        User[] expectedUsers = {testUser};

        when(userRepository.findByActivationStatusOrderByDisplayNameAsc(ActivationStatus.ACTIVE))
                .thenReturn(activeUsers);
        when(userConverter.toModel(activeUsers)).thenReturn(expectedUsers);

        User[] result = userService.getAll(true);

        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testUser);
    }

    @Test
    void get_ValidUserId_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserPO));
        when(userConverter.toModel(testUserPO)).thenReturn(testUser);

        User result = userService.get(1L);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    void get_NonExistentUserId_ShouldThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.get(999L));
    }
}
