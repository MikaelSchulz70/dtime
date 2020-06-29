package se.dtime.service.oncall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallConfigPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.error.ValidationException;
import se.dtime.model.oncall.OnCallDay;
import se.dtime.model.oncall.OnCallDayConfig;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.OnCallConfigRepository;
import se.dtime.repository.ProjectRepository;

@Service
public class OnCallValidator extends ValidatorBase<OnCallDay> {
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OnCallConfigRepository onCallConfigRepository;

    @Override
    public void validateAdd(OnCallDay onCallDay) {
        AssignmentPO assignmentPO = assignmentRepository.findById(onCallDay.getIdAssignment()).orElseThrow(() -> new ValidationException("oncall.assignment.not.found"));
        check(assignmentPO.getActivationStatus() == ActivationStatus.ACTIVE, "oncall.assignment.not.active");
        check(onCallDay.getDay() != null && onCallDay.getDay().getDate() != null, "oncall.date.not.specified");

        check(assignmentPO.getProject().getActivationStatus() == ActivationStatus.ACTIVE, "project.not.active");
        check(assignmentPO.getProject().isOnCall(), "project.not.oncall");

        if (onCallDay.isOnCall()) {
            OnCallConfigPO onCallConfigPO = onCallConfigRepository.findByProjectIdAndDayOfWeek(assignmentPO.getProject().getId(), onCallDay.getDay().getDate().getDayOfWeek());
            check(onCallConfigPO != null, "oncall.no.config.for.day");
            check(onCallConfigPO.getStartTime() != null || onCallConfigPO.getEndTime() != null, "oncall.no.config.for.day");
        }
    }

    @Override
    public void validateDelete(long idOnCall) {
    }

    @Override
    public void validateUpdate(OnCallDay onCallDay) {

    }

    public void validate(OnCallDayConfig onCallDayConfig) {
        ProjectPO projectPO = projectRepository.findById(onCallDayConfig.getIdProject()).orElseThrow(() -> new ValidationException("project.not.found"));
        check(projectPO.getActivationStatus() == ActivationStatus.ACTIVE, "project.not.active");
        check(projectPO.isOnCall(), "project.not.oncall");
        check(onCallDayConfig.getDayOfWeek() != null, "oncallconfig.day.not.found");
    }
}
