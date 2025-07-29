package se.dtime.service.user;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.CommonData;
import se.dtime.common.ValidatorBase;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserValidator extends ValidatorBase<User> {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskContributorRepository taskContributorRepository;
    @Autowired
    private TimeEntryRepository timeEntryRepository;

    static final String FIELD_FIRST_NAME = "firstName";
    static final String FIELD_LAST_NAME = "lastName";
    static final String FIELD_EMAIL = "email";
    static final String FIELD_PASSWORD = "password";
    static final String FIELD_ACTIVATION_STATUS = "activationStatus";
    static final String FIELD_ROLE = "userRole";

    private static Pattern REGEX_PATTERN_MOBILE_NUMBER = Pattern.compile("((\\+)(\\d{10,19}))");

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
            VALIDATOR_MAP.put(FIELD_FIRST_NAME, new FirstNameValidator());
            VALIDATOR_MAP.put(FIELD_LAST_NAME, new LastNameValidator());
            VALIDATOR_MAP.put(FIELD_EMAIL, new EmailValidator());
            VALIDATOR_MAP.put(FIELD_PASSWORD, new PasswordValidator());
            VALIDATOR_MAP.put(FIELD_ACTIVATION_STATUS, new ActivationStatusValidator());
            VALIDATOR_MAP.put(FIELD_ROLE, new UserRoleValidator());
        }
    }

    @Override
    public void validateAdd(User user) {
        validateEmail(user);
        validatePassword(user);
    }


    @Override
    public void validateDelete(long userId) {
        if (userId == CommonData.SYSTEM_USER_ID) {
            throw new ValidationException("user.delete.not.allowed");
        }

        UserPO storedUserPO = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));

        BigDecimal sumTime = timeEntryRepository.sumReportedTimeByUser(userId);
        check(sumTime == null || sumTime.compareTo(BigDecimal.ZERO) == 0, "user.cannot.delete.has.time.reports");

        validateAtLeastOneAdmin(storedUserPO);
    }

    @Override
    public void validateUpdate(User user) {
        if (user.getId() == CommonData.SYSTEM_USER_ID) {
            throw new ValidationException("user.update.not.allowed");
        }

        UserPO storedUserPO = userRepository.findById(user.getId()).orElseThrow(() -> new NotFoundException("user.not.found"));
        validateAtLeastOneAdmin(storedUserPO, user);
        validateEmail(user);
    }

    private void validateEmail(User user) {
        UserPO userPO = userRepository.findByEmail(user.getEmail());
        check(userPO == null || userPO.getId().equals(user.getId()), "user.email.not.unique");
    }


    private void validatePassword(User user) {
        // TODO add password strength rule
    }

    private void validateAtLeastOneAdmin(UserPO storedUserPO, User updatedUser) {
        if (storedUserPO.getActivationStatus() == ActivationStatus.ACTIVE &&
                storedUserPO.getUserRole() == UserRole.ADMIN &&
                (updatedUser.getActivationStatus() == ActivationStatus.INACTIVE ||
                        updatedUser.getUserRole() == UserRole.USER)) {
            List<UserPO> adminUser = userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE);
            boolean otherAdminExists = adminUser.stream().anyMatch(u -> u.getId() != storedUserPO.getId());
            check(otherAdminExists, "user.at.least.one.has.to.be.admin");
        }
    }

    public void validate(Attribute attribute) throws ValidationException {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

    public void validateAtLeastOneAdmin(UserPO userPO) {
        if (userPO.getActivationStatus() == ActivationStatus.ACTIVE &&
                userPO.getUserRole() == UserRole.ADMIN) {
            List<UserPO> adminUser = userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE);
            boolean otherAdminExists = adminUser.stream().anyMatch(u -> u.getId() != userPO.getId());
            check(otherAdminExists, "user.at.least.one.has.to.be.admin");
        }
    }


    class FirstNameValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 30, "user.first.name.length");
        }
    }

    class LastNameValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 30, "user.last.name.length");
        }
    }

    class EmailValidator extends AttributeValidator {
        private final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 60, "user.email.length");

            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(attribute.getValue());
            check(matcher.find(), attribute.getName(), "user.email.invalid");

            UserPO userPO = userRepository.findByEmail(attribute.getValue());
            check(userPO == null || userPO.getId() == attribute.getId(),
                    attribute.getName(), "user.email.not.unique");
        }
    }

    class PasswordValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 6, 80, "user.password.length");
        }
    }

    class ActivationStatusValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 30, "activation.status.invalid");

            try {
                ActivationStatus.valueOf(attribute.getValue());
            } catch (IllegalArgumentException e) {
                check(false, FIELD_ACTIVATION_STATUS, "activation.status.invalid");
            }
        }
    }

    class UserRoleValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 30, "user.user.role.invalid");

            try {
                UserRole.valueOf(attribute.getValue());
            } catch (IllegalArgumentException e) {
                check(false, FIELD_ROLE, "user.user.role.invalid");
            }
        }
    }
}
