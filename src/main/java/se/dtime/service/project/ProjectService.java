package se.dtime.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallConfigPO;
import se.dtime.dbmodel.oncall.OnCallPO;
import se.dtime.dbmodel.oncall.OnCallRulePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Project;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.*;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectConverter projectConverter;
    @Autowired
    private ProjectValidator projectValidator;
    @Autowired
    private OnCallConfigRepository onCallConfigRepository;
    @Autowired
    private OnCallRepository onCallRepository;
    @Autowired
    private OnCallRuleRepository onCallRuleRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private FixRateRepository fixRateRepository;
    @Autowired
    private OnCallAlarmRepository onCallAlarmRepository;
    @Autowired
    private TimeReportRepository timeReportRepository;

    public Project add(Project project) {
        projectValidator.validateAdd(project);
        ProjectPO projectPO = projectConverter.toPO(project);
        ProjectPO savedPO = projectRepository.save(projectPO);
        return projectConverter.toModel(savedPO);
    }

    public void update(Project project) {
        projectValidator.validateUpdate(project);
        ProjectPO updatedProject = projectConverter.toPO(project);
        projectRepository.save(updatedProject);

        if (updatedProject.isOnCall()) {
            List<OnCallConfigPO> onCallConfigPOS = onCallConfigRepository.findByProject(updatedProject);
            onCallConfigPOS.forEach(occ -> occ.setActivationStatus(updatedProject.getActivationStatus()));
            onCallConfigRepository.saveAll(onCallConfigPOS);
        } else {
            List<OnCallConfigPO> onCallConfigPOS = onCallConfigRepository.findByProject(updatedProject);
            onCallConfigRepository.deleteAll(onCallConfigPOS);

            List<OnCallPO> onCallPOS = onCallRepository.findByProject(project.getId());
            onCallRepository.deleteAll(onCallPOS);

            List<OnCallRulePO> onCallRulePOS = onCallRuleRepository.findByProject(updatedProject);
            onCallRuleRepository.deleteAll(onCallRulePOS);
        }

        if (project.getActivationStatus() == ActivationStatus.INACTIVE) {
            List<AssignmentPO> assignmentPOS = assignmentRepository.findByProject(updatedProject);
            assignmentPOS.forEach(a -> a.setActivationStatus(ActivationStatus.INACTIVE));
        }
    }


    public Project[] getAll(Boolean active, Boolean onCall) {
        List<ProjectPO> projectPOS = null;

        if (active == null && onCall == null) {
            projectPOS = projectRepository.findAll(Sort.by("company.name").ascending().and(Sort.by("name").ascending()));
        } else if (active != null && onCall == null) {
            ActivationStatus activationStatus = (active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE);
            projectPOS = projectRepository.findByActivationStatus(activationStatus);
        } else if (active != null) {
            ActivationStatus activationStatus = (active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE);
            projectPOS = projectRepository.findByOnCallTrueAndActivationStatus(activationStatus);
        } else {
            projectPOS = projectRepository.findByOnCallTrue();
        }

        return projectConverter.toModel(projectPOS);
    }

    public Project get(long id) {
        ProjectPO projectPO = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("project.not.found"));
        return projectConverter.toModel(projectPO);
    }

    public void delete(long idProject) {
        projectValidator.validateDelete(idProject);

        ProjectPO projectPO = new ProjectPO(idProject);
        timeReportRepository.deleteAll(timeReportRepository.findByProject(idProject));
        assignmentRepository.deleteAll(assignmentRepository.findByProject(projectPO));
        fixRateRepository.deleteAll(fixRateRepository.findByProject(projectPO));
        onCallAlarmRepository.deleteAll(onCallAlarmRepository.findByProject(projectPO));
        onCallConfigRepository.deleteAll(onCallConfigRepository.findByProject(projectPO));
        onCallRuleRepository.deleteAll(onCallRuleRepository.findByProject(projectPO));

        projectRepository.deleteById(idProject);
    }
}
