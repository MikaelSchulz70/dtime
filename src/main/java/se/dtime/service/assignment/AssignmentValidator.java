package se.dtime.service.assignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Assignment;
import se.dtime.model.Attribute;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.CompanyRepository;
import se.dtime.repository.ProjectRepository;
import se.dtime.repository.UserRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class AssignmentValidator extends ValidatorBase<Assignment> {
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;

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
    public void validateAdd(Assignment assignment) {
        UserPO userPO = userRepository.findById(assignment.getUser().getId()).orElseThrow(() -> new ValidationException("user.not.found"));
        check(userPO.getActivationStatus() == ActivationStatus.ACTIVE, "user.not.active");

        ProjectPO projectPO = projectRepository.findById(assignment.getProject().getId()).orElseThrow(() -> new ValidationException("project.not.found"));
        check(projectPO.getActivationStatus() == ActivationStatus.ACTIVE, "project.not.active");
    }

    @Override
    public void validateDelete(long idEntity) {

    }

    @Override
    public void validateUpdate(Assignment entity) {

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
            checkLength(attribute, 1, 40, "company.name.length");

            CompanyPO companyPO = companyRepository.findByName(attribute.getValue());
            check (companyPO == null || companyPO.getId() == attribute.getId(),
                    attribute.getName(), "company.name.not.unique");
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
