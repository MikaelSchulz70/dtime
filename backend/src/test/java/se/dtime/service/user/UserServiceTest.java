package se.dtime.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import se.dtime.common.CommonData;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.User;
import se.dtime.model.UserPwd;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TimeEntryRepository;
import se.dtime.repository.UserRepository;

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
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private TaskContributorRepository taskContributorRepository;
    @Mock
    private CloseDateRepository closeDateRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;

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
                .build();

        testUserPO = new UserPO();
        testUserPO.setId(1L);
        testUserPO.setFirstName("John");
        testUserPO.setLastName("Doe");
        testUserPO.setEmail("john.doe@example.com");
        testUserPO.setActivationStatus(ActivationStatus.ACTIVE);
        testUserPO.setPassword("encodedOldPassword");
    }

    @Test
    void add_ValidUser_ShouldReturnSavedUser() {
        // Given
        when(userConverter.toPO(testUser)).thenReturn(testUserPO);
        when(userRepository.save(testUserPO)).thenReturn(testUserPO);
        when(userConverter.toModel(testUserPO)).thenReturn(testUser);

        // When
        User result = userService.add(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userValidator).validateAdd(testUser);
        verify(userRepository).save(testUserPO);
    }

    @Test
    void update_ValidUser_ShouldUpdateUser() {
        // Given
        UserPO currentUserPO = new UserPO();
        currentUserPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUserPO));
        when(userConverter.toPO(testUser, currentUserPO)).thenReturn(testUserPO);

        // When
        assertDoesNotThrow(() -> userService.update(testUser));

        // Then
        verify(userValidator).validateUpdate(testUser);
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUserPO);
    }

    @Test
    void update_UserNotFound_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> userService.update(testUser));
        verify(userValidator).validateUpdate(testUser);
    }

    @Test
    void update_UserBecomesInactive_ShouldDeactivateTaskContributors() {
        // Given
        testUser.setActivationStatus(ActivationStatus.INACTIVE);

        UserPO currentUserPO = new UserPO();
        currentUserPO.setActivationStatus(ActivationStatus.ACTIVE);

        UserPO updatedUserPO = new UserPO();
        updatedUserPO.setActivationStatus(ActivationStatus.INACTIVE);

        TaskContributorPO taskContributor = new TaskContributorPO();
        taskContributor.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUserPO));
        when(userConverter.toPO(testUser, currentUserPO)).thenReturn(updatedUserPO);
        when(taskContributorRepository.findByUser(updatedUserPO))
                .thenReturn(Collections.singletonList(taskContributor));

        // When
        userService.update(testUser);

        // Then
        assertThat(taskContributor.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        verify(userRepository).save(updatedUserPO);
    }

    @Test
    void getAll_WithoutFilter_ShouldReturnAllUsersExceptSystem() {
        // Given
        UserPO systemUser = new UserPO();
        systemUser.setId(CommonData.SYSTEM_USER_ID);

        UserPO regularUser = new UserPO();
        regularUser.setId(1L);

        List<UserPO> allUsers = Arrays.asList(systemUser, regularUser);
        User[] expectedUsers = {testUser};

        when(userRepository.findAll(any(Sort.class))).thenReturn(allUsers);
        when(userConverter.toModel(Collections.singletonList(regularUser))).thenReturn(expectedUsers);

        // When
        User[] result = userService.getAll(null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testUser);
        verify(userRepository).findAll(any(Sort.class));
    }

    @Test
    void getAll_ActiveUsersOnly_ShouldReturnActiveUsers() {
        // Given
        List<UserPO> activeUsers = Collections.singletonList(testUserPO);
        User[] expectedUsers = {testUser};

        when(userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE))
                .thenReturn(activeUsers);
        when(userConverter.toModel(activeUsers)).thenReturn(expectedUsers);

        // When
        User[] result = userService.getAll(true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testUser);
        verify(userRepository).findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
    }

    @Test
    void getAll_InactiveUsersOnly_ShouldReturnInactiveUsers() {
        // Given
        List<UserPO> inactiveUsers = Collections.singletonList(testUserPO);
        User[] expectedUsers = {testUser};

        when(userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.INACTIVE))
                .thenReturn(inactiveUsers);
        when(userConverter.toModel(inactiveUsers)).thenReturn(expectedUsers);

        // When
        User[] result = userService.getAll(false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testUser);
        verify(userRepository).findByActivationStatusOrderByFirstNameAsc(ActivationStatus.INACTIVE);
    }

    @Test
    void get_ValidUserId_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUserPO));
        when(userConverter.toModel(testUserPO)).thenReturn(testUser);

        // When
        User result = userService.get(1L);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    void get_SystemUserId_ShouldThrowNotFoundException() {
        // When/Then
        assertThrows(NotFoundException.class,
                () -> userService.get(CommonData.SYSTEM_USER_ID));
    }

    @Test
    void get_NonExistentUserId_ShouldThrowNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> userService.get(999L));
    }

    @Test
    void delete_ValidUserId_ShouldDeleteUserAndRelatedData() {
        // Given
        when(timeEntryRepository.findByUser(1L)).thenReturn(Collections.emptyList());
        when(taskContributorRepository.findByUser(any(UserPO.class))).thenReturn(Collections.emptyList());
        when(closeDateRepository.findByUser(any(UserPO.class))).thenReturn(Collections.emptyList());

        // When
        assertDoesNotThrow(() -> userService.delete(1L));

        // Then
        verify(userValidator).validateDelete(1L);
        verify(timeEntryRepository).deleteAll(any());
        verify(taskContributorRepository).deleteAll(any());
        verify(closeDateRepository).deleteAll(any());
        verify(userRepository).deleteById(1L);
    }

    @Test
    void changePwd_ValidPasswordChange_ShouldUpdatePassword() {
        // Given
        UserPwd userPwd = UserPwd.builder()
                .currentPassword("oldPassword")
                .newPassword1("newPassword")
                .newPassword2("newPassword")
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(testUserPO);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // When
        assertDoesNotThrow(() -> userService.changePwd(userPwd));

        // Then
        verify(userValidator).validateLoggedIn();
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUserPO);
        assertThat(testUserPO.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void changePwd_InvalidCurrentPassword_ShouldThrowValidationException() {
        // Given
        UserPwd userPwd = UserPwd.builder()
                .currentPassword("wrongPassword")
                .newPassword1("newPassword")
                .newPassword2("newPassword")
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(testUserPO);
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.changePwd(userPwd));

        assertThat(exception.getFieldName()).isEqualTo("currentPassword");
        assertThat(exception.getMessage()).isEqualTo("user.invalid.current.pwd");
    }

    @Test
    void changePwd_PasswordsDoNotMatch_ShouldThrowValidationException() {
        // Given
        UserPwd userPwd = UserPwd.builder()
                .currentPassword("oldPassword")
                .newPassword1("newPassword1")
                .newPassword2("newPassword2")
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(testUserPO);
        when(passwordEncoder.matches("oldPassword", testUserPO.getPassword())).thenReturn(true);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.changePwd(userPwd));

        assertThat(exception.getFieldName()).isEqualTo("newPassword1");
        assertThat(exception.getMessage()).isEqualTo("user.new.pwd.do.not.match");
    }
}