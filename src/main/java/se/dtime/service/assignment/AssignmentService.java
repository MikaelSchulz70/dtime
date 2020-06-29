package se.dtime.service.assignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Assignment;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.ProjectRepository;
import se.dtime.repository.UserRepository;

import java.util.List;

@Service
public class AssignmentService {
    @Autowired
    private AssignmentConverter assignmentConverter;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private AssignmentValidator assignmentValidator;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    public Assignment addOrUpdate(Assignment assignment) {
        assignmentValidator.validateAdd(assignment);
        AssignmentPO assignmentPO = assignmentConverter.toPO(assignment);

        AssignmentPO storedAssignmentPO = assignmentRepository.findByUserAndProject(assignmentPO.getUser(), assignmentPO.getProject());
        if (storedAssignmentPO != null) {
            storedAssignmentPO.setActivationStatus(assignment.getActivationStatus());
            assignmentPO = storedAssignmentPO;
        }

        AssignmentPO savedPO = assignmentRepository.save(assignmentPO);
        return assignmentConverter.toModel(savedPO);
    }

    public List<Assignment> getAssignmentsForUser(long idUser) {
        UserPO userPO = userRepository.findById(idUser).orElseThrow(() -> new NotFoundException("user.not.found"));
        List<AssignmentPO> assignmentPOS = assignmentRepository.findByUserAndActivationStatus(userPO, ActivationStatus.ACTIVE);
        List<ProjectPO> projectPOS = projectRepository.findByActivationStatus(ActivationStatus.ACTIVE);
        return assignmentConverter.toModel(userPO, assignmentPOS, projectPOS);
    }

    public List<Assignment> getCurrentAssignments() {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getAssignmentsForUser(userExt.getId());
    }

    public void delete(long id) {
        assignmentRepository.deleteById(id);
    }
}
