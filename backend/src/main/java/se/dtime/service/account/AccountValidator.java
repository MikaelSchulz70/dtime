package se.dtime.service.account;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.AccountRepository;
import se.dtime.repository.TaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountValidator extends ValidatorBase<Account> {
    @Autowired
    private AccountRepository accountRepository;
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
    public void validateAdd(Account account) {
        validateName(account);
    }

    @Override
    public void validateDelete(long accountId) {
        AccountPO accountPO = accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("account.not.found"));
        checkNotFound(accountPO, "account.not.found");

        List<TaskPO> taskPOS = taskRepository.findByAccount(accountPO);
        check(taskPOS.isEmpty(), "account.cannot.delete.account.with.tasks");
    }

    @Override
    public void validateUpdate(Account account) {
        boolean exists = accountRepository.existsById(account.getId());
        checkNotFound(exists ? Boolean.TRUE : null, "account.not.found");

        validateName(account);
        validateInactivate(account);
    }

    private void validateInactivate(Account account) {
        if (account.getActivationStatus() != ActivationStatus.INACTIVE) {
            return;
        }

        List<TaskPO> taskPOS = taskRepository.findByAccount(new AccountPO(account.getId())).
                stream().
                filter(c -> c.getActivationStatus() == ActivationStatus.ACTIVE).
                toList();
        check(taskPOS.isEmpty(), "account.inactivation.not.allowed");
    }

    private void validateName(Account account) {
        AccountPO accountPO = accountRepository.findByName(account.getName());
        check(accountPO == null || account.getId() == accountPO.getId(), "account.name.not.unique");
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
