package se.dtime.service.assignment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.*;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.ProjectRepository;
import se.dtime.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignmentValidatorTest {
    @InjectMocks
    private AssignmentValidator assignmentValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        UserPO userPO = new UserPO(1L);
        userPO.setActivationStatus(ActivationStatus.ACTIVE);
        userPO.setUserRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));

        ProjectPO projectPO = new ProjectPO(1L);
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
    }

    @Test
    public void validateAddOk() {
        Assignment assignment = createAssignment();
        assignmentValidator.validateAdd(assignment);
    }

    @Test
    public void validateAddUserNotFound() {
        Assignment assignment = createAssignment();
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.not.found");

        assignmentValidator.validateAdd(assignment);
    }

    @Test
    public void validateAddUserInactive() {
        Assignment assignment = createAssignment();

        UserPO userPO = new UserPO(1L);
        userPO.setActivationStatus(ActivationStatus.INACTIVE);
        userPO.setUserRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.not.active");

        assignmentValidator.validateAdd(assignment);
    }

    @Test
    public void validateAddProjectNotFound() {
        Assignment assignment = createAssignment();
        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(null));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.found");

        assignmentValidator.validateAdd(assignment);
    }

    @Test
    public void validateAddProjectInactive() {
        Assignment assignment = createAssignment();

        ProjectPO projectPO = new ProjectPO(1L);
        projectPO.setActivationStatus(ActivationStatus.INACTIVE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.active");

        assignmentValidator.validateAdd(assignment);
    }

    private Assignment createAssignment() {
        Assignment assignment = new Assignment();
        assignment.setProject(Project.builder().id(1L).build());
        assignment.setUser(User.builder().id(1L).build());
        return assignment;
    }
}