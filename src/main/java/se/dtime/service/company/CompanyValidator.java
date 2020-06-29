package se.dtime.service.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.Company;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.CompanyRepository;
import se.dtime.repository.ProjectRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyValidator extends ValidatorBase<Company> {
    @Autowired
    private CompanyRepository companyRepository;
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
    public void validateAdd(Company company) {
        validateName(company);
    }

    @Override
    public void validateDelete(long idCompany) {
        CompanyPO companyPO = companyRepository.findById(idCompany).orElseThrow(() -> new NotFoundException("company.not.found"));
        checkNotFound(companyPO, "company.not.found");

        List<ProjectPO> projectPOS = projectRepository.findByCompany(companyPO);
        check(projectPOS.isEmpty(), "company.cannot.delete.company.with.projects");
    }

    @Override
    public void validateUpdate(Company company) {
        boolean exists = companyRepository.existsById(company.getId());
        checkNotFound(exists ? Boolean.TRUE : null, "company.not.found");

        validateName(company);
        validateInactivate(company);
    }

    private void validateInactivate(Company company) {
        if (company.getActivationStatus() != ActivationStatus.INACTIVE) {
            return;
        }

        List<ProjectPO> projectPOS = projectRepository.findByCompany(new CompanyPO(company.getId())).
                stream().
                filter(p -> p.getActivationStatus() == ActivationStatus.ACTIVE).
                collect(Collectors.toList());
        check(projectPOS.isEmpty(), "company.inactivation.not.allowed");
    }

    private void validateName(Company company) {
        CompanyPO companyPO = companyRepository.findByName(company.getName());
        check(companyPO == null || company.getId() == companyPO.getId(), "company.name.not.unique");
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
