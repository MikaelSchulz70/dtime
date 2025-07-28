package se.dtime.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.dtime.common.CommonData;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.User;
import se.dtime.model.UserRole;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TimeEntryRepository;
import se.dtime.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskContributorRepository taskContributorRepository;
    @Mock
    private TimeEntryRepository timeReportRepository;

    @BeforeEach
    public void setup() {
        reset(userRepository, taskContributorRepository, timeReportRepository);
        userValidator.init();
    }

    @Test
    public void validateFirstNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_FIRST_NAME).value("test1").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateFirstNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_FIRST_NAME).value("").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.first.name.length");
    }

    @Test
    public void validateFirstNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_FIRST_NAME).value("0123456789012345678901234567890").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.first.name.length");
    }

    @Disabled("TODO fix this test")
    @Test
    public void validateEmailNotUnique() {
        UserPO userPO = new UserPO();
        userPO.setId(2L);
        when(userRepository.findByEmail("a@a.se")).thenReturn(userPO);

        Attribute attribute = Attribute.builder().id(1).name(UserValidator.FIELD_EMAIL).value("a@a.se").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.email.not.unique");
    }


    @Test
    public void validateLastNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_LAST_NAME).value("okname").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateLastNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_LAST_NAME).value("").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.last.name.length");
    }

    @Test
    public void validateLastNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_LAST_NAME).value("0123456789012345678901234567890").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.last.name.length");
    }

    @Test
    public void validatePasswordOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_PASSWORD).value("123456").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateEmailOk() {
        Attribute attribute = Attribute.builder().id(3).name(UserValidator.FIELD_EMAIL).value("a@ab.se").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateEmailWrongFormat() {
        Attribute attribute = Attribute.builder().id(1).name(UserValidator.FIELD_EMAIL).value("aa.se").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.email.invalid");
    }

    @Test
    public void validatePasswordLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_PASSWORD).value("123").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.password.length");
    }

    @Test
    public void validatePasswordGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_PASSWORD).value("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.password.length");
    }

    @Test
    public void validateActivationStatusOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_ACTIVATION_STATUS).value("ACTIVE").build();
        userValidator.validate(attribute);

        attribute = Attribute.builder().id(0).name(UserValidator.FIELD_ACTIVATION_STATUS).value("INACTIVE").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateActivationStatusNotOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_ACTIVATION_STATUS).value("123").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("activation.status.invalid");
    }

    @Test
    public void validateUserRoleOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_ROLE).value("USER").build();
        userValidator.validate(attribute);

        attribute = Attribute.builder().id(0).name(UserValidator.FIELD_ROLE).value("ADMIN").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateUserRoleNotOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_ROLE).value("123").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validate(attribute);
        });
        assert exception.getMessage().contains("user.user.role.invalid");
    }

    @Test
    public void validateAddEmailExists() {
        when(userRepository.findByEmail("test@test.se")).thenReturn(new UserPO(2L));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validateAdd(User.builder().id(1L).email("test@test.se").build());
        });
        assert exception.getMessage().contains("user.email.not.unique");
    }

    @Test
    public void validateUpdateSystemUser() {
        assertThrows(ValidationException.class, () -> {
            userValidator.validateUpdate(User.builder().id(CommonData.SYSTEM_USER_ID).build());
        });
    }

    @Test
    public void validateUpdateUserNotFound() {
        assertThrows(NotFoundException.class, () -> {
            userValidator.validateUpdate(User.builder().id(2L).build());
        });
    }

    @Test
    public void validateUpdateAtLeastOneAdmin() {
        UserPO userPO = new UserPO(1L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE)).thenReturn(Arrays.asList());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validateUpdate(User.builder().id(1L).activationStatus(ActivationStatus.INACTIVE).build());
        });
        assert exception.getMessage().contains("user.at.least.one.has.to.be.admin");
    }

    @Test
    public void validateUpdateInactivate() {
        UserPO userPO = new UserPO(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));

        userValidator.validateUpdate(User.builder().id(1L).activationStatus(ActivationStatus.INACTIVE).build());
    }

    @Test
    public void validateUpdateEmailExists() {
        UserPO userPO = new UserPO(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByEmail("test@test.se")).thenReturn(new UserPO(4L));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validateUpdate(User.builder().id(1L).email("test@test.se").build());
        });
        assert exception.getMessage().contains("user.email.not.unique");
    }

    @Test
    public void validateUpdateOk() {
        UserPO userPO = new UserPO(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));

        userValidator.validateUpdate(User.builder().id(1L).activationStatus(ActivationStatus.INACTIVE).build());
    }

    @Test
    public void validateDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        assertThrows(NotFoundException.class, () -> {
            userValidator.validateDelete(1L);
        });
    }

    @Test
    public void validateDeleteSystemUser() {
        assertThrows(ValidationException.class, () -> {
            userValidator.validateDelete(CommonData.SYSTEM_USER_ID);
        });
    }

    @Test
    public void validateDeleteOnlyOneAdmin() {
        UserPO userPO = new UserPO(2L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE)).thenReturn(Arrays.asList(userPO));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validateDelete(1L);
        });
        assert exception.getMessage().contains("user.at.least.one.has.to.be.admin");
    }

    @Test
    public void validateDeleteHasReportedTime() {
        UserPO userPO = new UserPO(1L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(timeReportRepository.sumReportedTimeByUser(1L)).thenReturn(BigDecimal.TEN);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userValidator.validateDelete(1L);
        });
        assert exception.getMessage().contains("user.cannot.delete.has.time.reports");
    }

    @Test
    public void validateDeleteOk() {
        UserPO userPO = new UserPO(2L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE)).thenReturn(Arrays.asList(new UserPO(3L)));

        userValidator.validateDelete(1L);
    }
}