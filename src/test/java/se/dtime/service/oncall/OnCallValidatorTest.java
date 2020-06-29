package se.dtime.service.oncall;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallConfigPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.error.ValidationException;
import se.dtime.model.oncall.OnCallDay;
import se.dtime.model.oncall.OnCallDayConfig;
import se.dtime.model.timereport.Day;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.OnCallConfigRepository;
import se.dtime.repository.ProjectRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OnCallValidatorTest {

    @InjectMocks
    private OnCallValidator onCallValidator;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private OnCallConfigRepository onCallConfigRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        AssignmentPO assignmentPO = new AssignmentPO(1L);
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        ProjectPO projectPO = new ProjectPO(11L);
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        projectPO.setOnCall(true);
        assignmentPO.setProject(projectPO);

        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setDayOfWeek(DayOfWeek.THURSDAY);
        onCallConfigPO.setStartTime(LocalTime.of(12, 0));
        onCallConfigPO.setEndTime(LocalTime.of(13, 0));

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));
        when(onCallConfigRepository.findByProjectIdAndDayOfWeek(11, DayOfWeek.THURSDAY)).thenReturn(onCallConfigPO);
    }

    @Test
    public void validateAddOk() {
        OnCallDay onCallDay = createOnCallDay();
        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddInvalidAssignment() {
        OnCallDay onCallDay = createOnCallDay();

        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncall.assignment.not.found");

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddAssignmentNotActive() {
        OnCallDay onCallDay = createOnCallDay();

        AssignmentPO assignmentPO = new AssignmentPO(1L);
        assignmentPO.setActivationStatus(ActivationStatus.INACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncall.assignment.not.active");

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddInvalidDate() {
        OnCallDay onCallDay = createOnCallDay();

        onCallDay.getDay().setDate(null);
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncall.date.not.specified");

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddProjectNotActive() {
        OnCallDay onCallDay = createOnCallDay();

        AssignmentPO assignmentPO = new AssignmentPO(1L);
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.INACTIVE);
        projectPO.setOnCall(true);
        assignmentPO.setProject(projectPO);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.active");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddProjectNotOnCall() {
        OnCallDay onCallDay = createOnCallDay();

        AssignmentPO assignmentPO = new AssignmentPO(1L);
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        projectPO.setOnCall(false);
        assignmentPO.setProject(projectPO);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.oncall");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddNoOnCallAndNoConfigFound() {
        OnCallDay onCallDay = createOnCallDay();
        onCallDay.setOnCall(false);
        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddNoConfigFound() {
        OnCallDay onCallDay = createOnCallDay();

        when(onCallConfigRepository.findByProjectIdAndDayOfWeek(11, DayOfWeek.THURSDAY)).thenReturn(null);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncall.no.config.for.day");

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateAddNoStartAndEndTimeConfig() {
        OnCallDay onCallDay = createOnCallDay();

        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setDayOfWeek(DayOfWeek.THURSDAY);

        when(onCallConfigRepository.findByProjectIdAndDayOfWeek(11, DayOfWeek.THURSDAY)).thenReturn(onCallConfigPO);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncall.no.config.for.day");

        onCallValidator.validateAdd(onCallDay);
    }

    @Test
    public void validateOnCallDayConfigOk() {
        OnCallDayConfig onCallDayConfig = createOnCallDayConfig();
        onCallValidator.validate(onCallDayConfig);
    }

    @Test
    public void validateOnCallDayConfigProjectNotFound() {
        OnCallDayConfig onCallDayConfig = createOnCallDayConfig();

        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.found");

        onCallValidator.validate(onCallDayConfig);
    }

    @Test
    public void validateOnCallDayConfigProjectInactive() {
        OnCallDayConfig onCallDayConfig = createOnCallDayConfig();

        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.INACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.active");

        onCallValidator.validate(onCallDayConfig);
    }

    @Test
    public void validateOnCallDayConfigProjectNotOnCall() {
        OnCallDayConfig onCallDayConfig = createOnCallDayConfig();

        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        projectPO.setOnCall(false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.oncall");

        onCallValidator.validate(onCallDayConfig);
    }

    @Test
    public void validateOnCallDayConfigNoDayOfWeek() {
        OnCallDayConfig onCallDayConfig = createOnCallDayConfig();
        onCallDayConfig.setDayOfWeek(null);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncallconfig.day.not.found");

        onCallValidator.validate(onCallDayConfig);
    }

    private OnCallDayConfig createOnCallDayConfig() {
        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        projectPO.setOnCall(true);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));

        return OnCallDayConfig.builder().idProject(1).dayOfWeek(DayOfWeek.MONDAY).build();
    }

    private OnCallDay createOnCallDay() {
        LocalDate date = LocalDate.of(2019, 1, 24);
        return OnCallDay.builder().day(Day.builder().date(date).build()).idAssignment(1).isOnCall(true).build();
    }
}