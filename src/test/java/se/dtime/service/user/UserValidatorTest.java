package se.dtime.service.user;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.common.CommonData;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.User;
import se.dtime.model.UserRole;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.TimeReportRepository;
import se.dtime.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private TimeReportRepository timeReportRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        userValidator.init();

        UserPO userPO = new UserPO();
        userPO.setId(2L);
        when(userRepository.findByUserName("test")).thenReturn(userPO);
        when(userRepository.findByEmail("a@a.se")).thenReturn(userPO);
    }

    @Test
    public void validateUserNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_USER_NAME).value("test1").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateUserNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_USER_NAME).value("").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.username.length");
        userValidator.validate(attribute);
    }

    @Test
    public void validateUserNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_USER_NAME).value("0123456789012345678901234567890").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.username.length");
        userValidator.validate(attribute);
    }

    @Test
    public void validateUserNameNotUnique() {
        Attribute attribute = Attribute.builder().id(1).name(UserValidator.FIELD_USER_NAME).value("test").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.username.not.unique");
        userValidator.validate(attribute);
    }

    @Test
    public void validateFirstNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_FIRST_NAME).value("okname").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateFirstNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_FIRST_NAME).value("").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.first.name.length");
        userValidator.validate(attribute);
    }

    @Test
    public void validateFirstNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_FIRST_NAME).value("0123456789012345678901234567890").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.first.name.length");
        userValidator.validate(attribute);
    }

    @Test
    public void validateLastNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_LAST_NAME).value("okname").build();
        userValidator.validate(attribute);
    }

    @Test
    public void validateLastNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_LAST_NAME).value("").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.last.name.length");
        userValidator.validate(attribute);
    }

    @Test
    public void validateLastNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_LAST_NAME).value("0123456789012345678901234567890").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.last.name.length");
        userValidator.validate(attribute);
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
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.email.invalid");
        userValidator.validate(attribute);
    }

    @Test
    public void validateEmailNotUnique() {
        Attribute attribute = Attribute.builder().id(1).name(UserValidator.FIELD_EMAIL).value("a@a.se").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.email.not.unique");
        userValidator.validate(attribute);
    }

    @Test
    public void validatePasswordLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_PASSWORD).value("123").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.password.length");
        userValidator.validate(attribute);
    }

    @Test
    public void validatePasswordGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_PASSWORD).value("012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.password.length");
        userValidator.validate(attribute);
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
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("activation.status.invalid");
        userValidator.validate(attribute);
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
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.user.role.invalid");
        userValidator.validate(attribute);
    }

    @Test
    public void validateAddUserNameExists() {
        when(userRepository.findByUserName("test")).thenReturn(new UserPO(2L));

        expectedException.expectMessage("user.username.not.unique");
        expectedException.expect(ValidationException.class);
        userValidator.validateAdd(User.builder().id(1L).userName("test").build());
    }

    @Test
    public void validateMobileNumberExists() {
        when(userRepository.findByMobileNumber("+46733347909")).thenReturn(new UserPO(1L));

        expectedException.expectMessage("user.mobile.number.not.unique");
        expectedException.expect(ValidationException.class);
        userValidator.validateAdd(User.builder().id(2L).mobileNumber("+46733347909").build());
    }

    @Test
    public void validateMobileNumberOk() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_MOBILE_NUMBER).build();

        attribute.setValue("+46733347909");
        userValidator.validate(attribute);

        attribute.setValue("+46 733 34 79 09");
        userValidator.validate(attribute);

        attribute.setValue("+46 733 347 909");
        userValidator.validate(attribute);

        attribute.setValue("+45 71234567");
        userValidator.validate(attribute);
    }

    @Test
    public void validateMobileNumberTooShort() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_MOBILE_NUMBER).value("+467334").build();
        expectedException.expectMessage("user.user.mobile.number.invalid");
        expectedException.expect(ValidationException.class);
        userValidator.validate(attribute);
    }

    @Test
    public void validateMobileNumberTooLong() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_MOBILE_NUMBER).value("+46 7 3334 79 09 12345").build();
        expectedException.expectMessage("user.user.mobile.number.invalid");
        expectedException.expect(ValidationException.class);
        userValidator.validate(attribute);
    }

    @Test
    public void validateMobileNumberNoStartingPlus() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_MOBILE_NUMBER).value("46 733 347 909").build();
        expectedException.expectMessage("user.user.mobile.number.invalid");
        expectedException.expect(ValidationException.class);
        userValidator.validate(attribute);
    }

    @Test
    public void validateMobileNumberInvalidChar() {
        Attribute attribute = Attribute.builder().id(0).name(UserValidator.FIELD_MOBILE_NUMBER).value("+46 A33 347 909").build();
        expectedException.expectMessage("user.user.mobile.number.invalid");
        expectedException.expect(ValidationException.class);
        userValidator.validate(attribute);
    }

    @Test
    public void validateAddEmailExists() {
        when(userRepository.findByEmail("b@b.se")).thenReturn(new UserPO(2L));

        expectedException.expectMessage("user.email.not.unique");
        expectedException.expect(ValidationException.class);
        userValidator.validateAdd(User.builder().id(1L).email("b@b.se").build());
    }

    @Test
    public void validateUpdateSystemUser() {
        expectedException.expect(ValidationException.class);
        userValidator.validateUpdate(User.builder().id(CommonData.SYSTEM_USER_ID).build());
    }

    @Test
    public void validateUpdateUserNotFound() {
        expectedException.expect(NotFoundException.class);
        userValidator.validateUpdate(User.builder().id(2L).build());
    }

    @Test
    public void validateUpdateAtLeastOneAdmin() {
        UserPO userPO = new UserPO(1L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE)).thenReturn(Arrays.asList());

        expectedException.expectMessage("user.at.least.one.has.to.be.admin");
        expectedException.expect(ValidationException.class);
        userValidator.validateUpdate(User.builder().id(1L).activationStatus(ActivationStatus.INACTIVE).build());
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
        when(userRepository.findByEmail("a@a.se")).thenReturn(new UserPO(4L));

        expectedException.expectMessage("user.email.not.unique");
        expectedException.expect(ValidationException.class);
        userValidator.validateUpdate(User.builder().id(1L).email("a@a.se").build());
    }

    @Test
    public void validateUpdateUserNameExists() {
        UserPO userPO = new UserPO(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByUserName("test")).thenReturn(new UserPO(4L));

        expectedException.expectMessage("user.username.not.unique");
        expectedException.expect(ValidationException.class);
        userValidator.validateUpdate(User.builder().id(1L).userName("test").build());
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
        expectedException.expect(NotFoundException.class);
        userValidator.validateDelete(1L);
    }

    @Test
    public void validateDeleteSystemUser() {
        expectedException.expect(ValidationException.class);
        userValidator.validateDelete(CommonData.SYSTEM_USER_ID);
    }

    @Test
    public void validateDeleteOnlyOneAdmin() {
        UserPO userPO = new UserPO(2L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE)).thenReturn(Arrays.asList(userPO));

        expectedException.expectMessage("user.at.least.one.has.to.be.admin");
        expectedException.expect(ValidationException.class);
        userValidator.validateDelete(1L);
    }

    @Test
    public void validateDeleteHasReportedTime() {
        UserPO userPO = new UserPO(1L);
        userPO.setUserRole(UserRole.ADMIN);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));
        when(timeReportRepository.sumReportedTimeByUser(1L)).thenReturn(BigDecimal.TEN);

        expectedException.expectMessage("user.cannot.delete.has.time.reports");
        expectedException.expect(ValidationException.class);
        userValidator.validateDelete(1L);
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