package se.dtime.service.oncall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallRulePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.error.ValidationException;
import se.dtime.model.oncall.OnCallRule;
import se.dtime.repository.OnCallRuleRepository;
import se.dtime.repository.ProjectRepository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OnCallRuleValidator extends ValidatorBase<OnCallRule> {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OnCallRuleRepository onCallRuleRepository;

    static final String FIELD_FROM_MAIL = "fromMail";

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
            VALIDATOR_MAP.put(FIELD_FROM_MAIL, new OnCallRuleValidator.FromMailValidator());
        }
    }

    @Override
    public void validateAdd(OnCallRule onCallRule) {
        check(onCallRule.getProject() != null, "project.not.found");
        ProjectPO projectPO = projectRepository.findById(onCallRule.getProject().getId()).orElseThrow(() -> new ValidationException("project.not.found"));
        check(projectPO.getActivationStatus() == ActivationStatus.ACTIVE, "project.not.active");
        check(projectPO.isOnCall(), "project.not.oncall");
        check(checkFromEmail(onCallRule.getFromMail()), "oncall.rule.invalid.sender.email");

        OnCallRulePO onCallRulePO = onCallRuleRepository.findByFromEmail(onCallRule.getFromMail());
        check (onCallRulePO == null || onCallRulePO.getId() == onCallRule.getId(), "oncall.rule.sender.email.already.in.use");
    }

    @Override
    public void validateDelete(long idOnCallRule) {
    }

    @Override
    public void validateUpdate(OnCallRule onCallRule) {

    }

    boolean checkFromEmail(String fromEmail) {
        if (StringUtils.isEmpty(fromEmail)) {
            return false;
        }

        Pattern regex = Pattern.compile(".*@[-0-9a-zA-Z.+_]+\\..+");
        Matcher matcher = regex.matcher(fromEmail);
        return matcher.matches();
    }

    public void validate(Attribute attribute) {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

    class FromMailValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 4, 60, "oncall.rule.sender.mail.lenght");

            check(checkFromEmail(attribute.getValue()), attribute.getName(),"oncall.rule.invalid.sender.email");

            OnCallRulePO onCallRulePO = onCallRuleRepository.findByFromEmail(attribute.getValue());
            check (onCallRulePO == null || onCallRulePO.getId() == attribute.getId(),
                    attribute.getName(), "oncall.rule.sender.email.already.in.use");
        }
    }

}
