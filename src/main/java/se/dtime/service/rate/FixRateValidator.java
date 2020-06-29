package se.dtime.service.rate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.FixRatePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.Attribute;
import se.dtime.model.FixRate;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.FixRateRepository;
import se.dtime.repository.ProjectRepository;
import se.dtime.utils.RateValidatorUtil;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FixRateValidator extends ValidatorBase<FixRate> {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FixRateRepository fixRateRepository;

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
        }
    }

    @Override
    public void validateAdd(FixRate fixRate) {
        check(fixRate.getIdProject() != 0, "project.not.found");
        ProjectPO projectPO = projectRepository.findById(fixRate.getIdProject()).orElseThrow(() -> new NotFoundException("project.not.found"));

        check(fixRate.getRate() != null, "rate.customer.rate.not.specified");
        check(fixRate.getFromDate() != null, "rate.from.date.not.specified");
        if (fixRate.getToDate() != null) {
            check(fixRate.getFromDate().isBefore(fixRate.getToDate()), "rate.from.date.is.after.to.date");
        }

        check(projectPO.isFixRate(), "fixrate.fix.rate.cannot.be.on.hour.rate.project");

        List<FixRatePO> fixRatePOS = fixRateRepository.findByProjectOrderByFromDateDesc(projectPO);
        validateAlreadyActiveRate(fixRate, fixRatePOS);
        validateDateRange(fixRate, fixRatePOS);
    }

    private void validateAlreadyActiveRate(FixRate fixRate, List<FixRatePO> fixRatePOS) {
        if (fixRate.getToDate() == null) {
            boolean isActiveRate = fixRatePOS.stream().anyMatch(r -> r.getToDate() == null && r.getId() != fixRate.getId());
            check(!isActiveRate, "rate.already.an.active.rate");
        }
    }

    void validateDateRange(FixRate fixRate, List<FixRatePO> fixRatePOS) {
        for (FixRatePO fixRatePO : fixRatePOS) {
            if (fixRate.getId() == fixRatePO.getId()) {
                continue;
            }

            check(!RateValidatorUtil.isDateRangeOverlapping(fixRate.getFromDate(),
                    fixRate.getToDate(), fixRatePO.getFromDate(), fixRatePO.getToDate()),
                    "rate.overlapping.date.range");
        }
    }

    @Override
    public void validateDelete(long idProject) {

    }

    @Override
    public void validateUpdate(FixRate fixRate) {
        validateAdd(fixRate);
    }

    public void validate(Attribute attribute) throws ValidationException {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

}
