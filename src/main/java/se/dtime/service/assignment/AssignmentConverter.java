package se.dtime.service.assignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Assignment;
import se.dtime.model.User;
import se.dtime.service.BaseConverter;
import se.dtime.service.project.ProjectConverter;
import se.dtime.service.user.UserConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentConverter extends BaseConverter {
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private ProjectConverter projectConverter;

    public Assignment toModel(AssignmentPO assignmentPO) {
        if (assignmentPO == null) {
            return null;
        }

        return Assignment.builder().id(assignmentPO.getId()).
                user(userConverter.toModel(assignmentPO.getUser())).
                project(projectConverter.toModel(assignmentPO.getProject())).
                build();
    }

    public List<Assignment> toModel(List<AssignmentPO> assignmentPOList) {
        return assignmentPOList.stream().map(c -> toModel(c)).collect(Collectors.toList());
    }

    public AssignmentPO toPO(Assignment assignment) {
        if (assignment == null) {
            return null;
        }

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setId(assignment.getId());
        assignmentPO.setUser(new UserPO(assignment.getUser().getId()));
        assignmentPO.setProject(new ProjectPO(assignment.getProject().getId()));
        assignmentPO.setActivationStatus(assignment.getActivationStatus());
        updateBaseData(assignmentPO);

        return assignmentPO;
    }

    public List<Assignment> toModel(UserPO userPO, List<AssignmentPO> assignmentPOS, List<ProjectPO> projectPOS) {
        User user = userConverter.toModel(userPO);

        List<Assignment> assignments = new ArrayList<>();
        for (ProjectPO projectPO : projectPOS) {
            Optional<AssignmentPO> assignmentPO = assignmentPOS.
                    stream().
                    filter(x -> x.getProject().getId() == projectPO.getId()).
                    findFirst();

            Assignment assignment = Assignment.builder().
                    id(0L).
                    project(projectConverter.toModel(projectPO)).
                    user(user).
                    activationStatus(assignmentPO.isPresent() ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE).
                    build();

            assignments.add(assignment);
        }

        return assignments;
    }
}
