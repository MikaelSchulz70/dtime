package se.dtime.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.FixRatePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.RatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.Project;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.*;
import se.dtime.utils.RateValidatorUtil;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectValidator extends ValidatorBase<Project> {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private TimeReportRepository timeReportRepository;
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private FixRateRepository fixRateRepository;


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
    public void validateAdd(Project project) {
        validateName(project);
    }

    @Override
    public void validateDelete(long idProject) {
        ProjectPO projectPO = projectRepository.findById(idProject).orElseThrow(() -> new NotFoundException("project.not.found"));
        validateHasTimeReports(projectPO);
    }

    @Override
    public void validateUpdate(Project project) {
        ProjectPO projectPO = projectRepository.findById(project.getId()).orElseThrow(() -> new NotFoundException("project.not.found"));

        validateName(project);
        validateRate(project, projectPO);
    }

    void validateRate(Project project, ProjectPO projectPO) {
        if (project.isFixRate() == projectPO.isFixRate()) {
            return;
        }

        List<RatePO> ratePOS = rateRepository.findByProject(projectPO.getId());
        List<FixRatePO> fixRatePOS = fixRateRepository.findByProject(projectPO);

        if (project.isFixRate()) {
            check(allRatesClosed(ratePOS), "project.open.rate");
        } else {
            check(allFixRatesClosed(fixRatePOS), "project.open.fix.rate");
        }

        validateOverlap(ratePOS, fixRatePOS);
    }

    private void validateOverlap(List<RatePO> ratePOS, List<FixRatePO> fixRatePOS) {
        for (RatePO ratePO : ratePOS) {
            for (FixRatePO fixRatePO : fixRatePOS) {
                check(!RateValidatorUtil.isDateRangeOverlapping(ratePO.getFromDate(), ratePO.getToDate(), fixRatePO.getFromDate(), fixRatePO.getToDate()),
                        "project.overlapping.rates");
            }
        }
    }

    private boolean allFixRatesClosed(List<FixRatePO> fixRatePOS) {
        return fixRatePOS.stream().noneMatch(r -> r.getToDate() == null);
    }

    private boolean allRatesClosed(List<RatePO> ratePOS) {
        return ratePOS.stream().noneMatch(r -> r.getToDate() == null);
    }

    private void validateName(Project project) {
        List<ProjectPO> projectPOs = projectRepository.findByName(project.getName());
        check(projectPOs == null || projectPOs.size() == 0 ||
                        projectPOs.stream().noneMatch(p -> !p.getId().equals(project.getId()) &&
                                p.getCompany().getId().equals(project.getCompany().getId())),
                "project.name.not.unique");
    }

    private void validateHasTimeReports(ProjectPO projectPO) {
        BigDecimal totalTime = timeReportRepository.sumReportedTimeByProject(projectPO.getId());
        check(totalTime == null || totalTime.compareTo(BigDecimal.ZERO) == 0, "project.cannot.delete.have.time.reports");
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
            checkLength(attribute, 1, 40, "project.name.length");
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
