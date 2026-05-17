package se.dtime.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.User;
import se.dtime.model.UserRole;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;

    @Mock
    private UserRepository userRepository;

    @Test
    void validateAdd_notSupported() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userValidator.validateAdd(User.builder().email("test@test.se").build()));
        assert exception.getMessage().contains("user.create.not.supported");
    }

    @Test
    void validateUpdate_notSupported() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userValidator.validateUpdate(User.builder().id(1L).build()));
        assert exception.getMessage().contains("user.update.not.supported");
    }

    @Test
    void validateDelete_notSupported() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userValidator.validateDelete(1L));
        assert exception.getMessage().contains("user.delete.not.supported");
    }

    @Test
    void validateDeactivate_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userValidator.validateDeactivate(2L));
    }

    @Test
    void validateDeactivate_alreadyInactive() {
        UserPO user = inactiveUser(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userValidator.validateDeactivate(1L));
        assert exception.getMessage().contains("user.already.inactive");
    }

    @Test
    void validateDeactivate_lastActiveAdmin_notAllowed() {
        UserPO user = activeUser(1L, UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE))
                .thenReturn(List.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userValidator.validateDeactivate(1L));
        assert exception.getMessage().contains("user.at.least.one.has.to.be.admin");
    }

    @Test
    void validateDeactivate_activeUser_ok() {
        UserPO user = activeUser(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userValidator.validateDeactivate(1L));
    }

    @Test
    void validateDeactivate_activeAdminWithOtherAdmin_ok() {
        UserPO user = activeUser(1L, UserRole.ADMIN);
        UserPO otherAdmin = activeUser(2L, UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE))
                .thenReturn(List.of(user, otherAdmin));

        assertDoesNotThrow(() -> userValidator.validateDeactivate(1L));
    }

    @Test
    void validateActivate_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userValidator.validateActivate(2L));
    }

    @Test
    void validateActivate_alreadyActive() {
        UserPO user = activeUser(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userValidator.validateActivate(1L));
        assert exception.getMessage().contains("user.already.active");
    }

    @Test
    void validateActivate_inactiveUser_ok() {
        UserPO user = inactiveUser(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userValidator.validateActivate(1L));
    }

    private static UserPO activeUser(long id, UserRole role) {
        UserPO user = new UserPO(id);
        user.setUserRole(role);
        user.setActivationStatus(ActivationStatus.ACTIVE);
        return user;
    }

    private static UserPO inactiveUser(long id, UserRole role) {
        UserPO user = new UserPO(id);
        user.setUserRole(role);
        user.setActivationStatus(ActivationStatus.INACTIVE);
        return user;
    }
}
