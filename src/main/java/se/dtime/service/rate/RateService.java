package se.dtime.service.rate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.RatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Rate;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.RateRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RateService {

    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private RateConverter rateConverter;
    @Autowired
    private RateValidator rateValidator;

    public void add(Rate rate) {
        rateValidator.validateAdd(rate);
        RatePO ratePO = rateConverter.toPO(rate);
        rateRepository.save(ratePO);
    }

    public void update(Rate rate) {
        rateValidator.validateUpdate(rate);
        RatePO updatedRate = rateConverter.toPO(rate);
        rateRepository.save(updatedRate);
    }

    public Rate[] getCurrentRates() {
        List<AssignmentPO> assignmentPOS = assignmentRepository.findByActivationStatus(ActivationStatus.ACTIVE).
                stream().filter(a -> !a.getProject().isInternal() && !a.getProject().isFixRate()).collect(Collectors.toList());
        List<RatePO> ratePOS = rateRepository.findCurrentRates();
        return rateConverter.toModel(assignmentPOS, ratePOS);
    }

    public Rate[] getRatesForAssignment(long idAssignment) {
        AssignmentPO assignmentPO = assignmentRepository.findById(idAssignment).orElseThrow(() -> new NotFoundException("assignment.not.found"));
        List<RatePO> ratePOS = rateRepository.findByAssignmentOrderByFromDateDesc(assignmentPO);

        // Add empty rate for possible new rate
        RatePO ratePO = new RatePO(0L);
        ratePO.setAssignment(assignmentPO);
        ratePOS.add(ratePO);

        return rateConverter.toModel(ratePOS);
    }

    public Rate get(long id) {
        RatePO ratePO = rateRepository.findById(id).orElseThrow(() -> new NotFoundException("rate.not.found"));
        return rateConverter.toModel(ratePO);
    }


    public void delete(long id) {
        rateValidator.validateDelete(id);
        rateRepository.deleteById(id);
    }
}
