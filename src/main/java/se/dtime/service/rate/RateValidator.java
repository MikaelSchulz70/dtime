package se.dtime.service.rate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.RatePO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.Attribute;
import se.dtime.model.Rate;
import se.dtime.model.UserCategory;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.RateRepository;
import se.dtime.repository.UserRepository;
import se.dtime.utils.RateValidatorUtil;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RateValidator extends ValidatorBase<Rate> {
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private UserRepository userRepository;

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
        }
    }

    @Override
    public void validateAdd(Rate rate) {
        check(rate.getIdAssignment() != 0, "assignment.not.found");
        AssignmentPO assignmentPO = assignmentRepository.findById(rate.getIdAssignment()).orElseThrow(() -> new NotFoundException("assignment.not.found"));
        ;
        check(rate.getIdUser() != 0, "user.not.found");

        UserPO userPO = userRepository.findById(rate.getIdUser()).orElseThrow(() -> new NotFoundException("user.not.found"));

        if (userPO.getUserCategory() == UserCategory.SUBCONTRACTOR) {
            check(rate.getSubcontractorRate() != null, "rate.no.subcontractor.rate");
        } else {
            check(rate.getSubcontractorRate() == null, "rate.only.subcontractor.can.have.subcontractor.rate");
        }

        check(rate.getCustomerRate() != null, "rate.customer.rate.not.specified");
        check(rate.getFromDate() != null, "rate.from.date.not.specified");
        if (rate.getToDate() != null) {
            check(rate.getFromDate().isBefore(rate.getToDate()), "rate.from.date.is.after.to.date");
        }

        check(!assignmentPO.getProject().isFixRate(), "rate.hour.rate.cannot.be.on.fix.rate.project");

        List<RatePO> ratePOS = rateRepository.findByAssignmentOrderByFromDateDesc(assignmentPO);
        validateAlreadyActiveRate(rate, ratePOS);
        validateDateRange(rate, ratePOS);
    }

    private void validateAlreadyActiveRate(Rate rate, List<RatePO> ratePOS) {
        if (rate.getToDate() == null) {
            boolean isActiveRate = ratePOS.stream().anyMatch(r -> r.getToDate() == null && r.getId() != rate.getId());
            check(!isActiveRate, "rate.already.an.active.rate");
        }
    }

    void validateDateRange(Rate rate, List<RatePO> ratePOS) {
        for (RatePO ratePO : ratePOS) {
            if (rate.getId() == ratePO.getId()) {
                continue;
            }

            check(!RateValidatorUtil.isDateRangeOverlapping(rate.getFromDate(),
                    rate.getToDate(), ratePO.getFromDate(), ratePO.getToDate()),
                    "rate.overlapping.date.range");
        }
    }

    @Override
    public void validateDelete(long idProject) {

    }

    @Override
    public void validateUpdate(Rate rate) {
        validateAdd(rate);
    }

    public void validate(Attribute attribute) throws ValidationException {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

}
