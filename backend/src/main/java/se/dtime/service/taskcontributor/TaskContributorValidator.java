package se.dtime.service.taskcontributor;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.TaskContributor;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.AccountRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskContributorValidator extends ValidatorBase<TaskContributor> {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    static final String FIELD_NAME = "name";
    static final String FIELD_ACTIVATION_STATUS = "activationStatus";

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
            VALIDATOR_MAP.put(FIELD_NAME, new NameValidator());
            VALIDATOR_MAP.put(FIELD_ACTIVATION_STATUS, new ActivationStatusValidator());
        }
    }

    @Override
    public void validateAdd(TaskContributor taskContributor) {
        UserPO userPO = userRepository.findById(taskContributor.getUser().getId()).orElseThrow(() -> new ValidationException("user.not.found"));
        check(userPO.getActivationStatus() == ActivationStatus.ACTIVE, "user.not.active");

        TaskPO taskPO = taskRepository.findById(taskContributor.getTask().getId()).orElseThrow(() -> new ValidationException("task.not.found"));
        check(taskPO.getActivationStatus() == ActivationStatus.ACTIVE, "task.not.active");
    }

    @Override
    public void validateDelete(long idEntity) {

    }

    @Override
    public void validateUpdate(TaskContributor entity) {

    }

    public void validate(Attribute attribute) throws ValidationException {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

    class NameValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 40, "account.name.length");

            AccountPO accountPO = accountRepository.findByName(attribute.getValue());
            check(accountPO == null || accountPO.getId() == attribute.getId(),
                    attribute.getName(), "account.name.not.unique");
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
}
