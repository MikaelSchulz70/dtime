package se.dtime.service.oncall;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallRulePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Project;
import se.dtime.model.error.ValidationException;
import se.dtime.model.oncall.OnCallRule;
import se.dtime.repository.OnCallRuleRepository;
import se.dtime.repository.ProjectRepository;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OnCallRuleValidatorTest {
    @InjectMocks
    private OnCallRuleValidator onCallRuleValidator;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private OnCallRuleRepository onCallRuleRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateOnCallRuleOk() {
        OnCallRule onCallRule = createOnCallRule();
        onCallRuleValidator.validateAdd(onCallRule);
    }

    @Test
    public void validateOnCallRuleProjectNotFound() {
        OnCallRule onCallRule = createOnCallRule();

        when(projectRepository.findById(1L)).thenReturn(Optional.ofNullable(null));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.found");

        onCallRuleValidator.validateAdd(onCallRule);
    }

    @Test
    public void validateOnCallRuleProjectInactive() {
        OnCallRule onCallRule = createOnCallRule();

        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.INACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.active");

        onCallRuleValidator.validateAdd(onCallRule);
    }

    @Test
    public void validateOnCallRuleProjectNotOnCall() {
        OnCallRule onCallRule = createOnCallRule();

        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        projectPO.setOnCall(false);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.not.oncall");

        onCallRuleValidator.validateAdd(onCallRule);
    }

    @Test
    public void validateOnCallRuleFromEmailInUse() {
        OnCallRule onCallRule = createOnCallRule();
        onCallRuleValidator.validateAdd(onCallRule);

        when(onCallRuleRepository.findByFromEmail(anyString())).thenReturn(new OnCallRulePO(2L));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("oncall.rule.sender.email.already.in.use");

        onCallRuleValidator.validateAdd(onCallRule);
    }


    @Test
    public void validateOnCallRuleInvalidFromEmail() {
        assertTrue(onCallRuleValidator.checkFromEmail("info@dtime.se"));
        assertTrue(onCallRuleValidator.checkFromEmail("@dtime.se"));
        assertTrue(onCallRuleValidator.checkFromEmail("@dtime.se.com"));
        assertFalse(onCallRuleValidator.checkFromEmail("dtime.se"));
        assertFalse(onCallRuleValidator.checkFromEmail(null));
        assertFalse(onCallRuleValidator.checkFromEmail(""));
        assertFalse(onCallRuleValidator.checkFromEmail("@"));
        assertFalse(onCallRuleValidator.checkFromEmail("@dtime"));
    }

    private OnCallRule createOnCallRule() {
        ProjectPO projectPO = new ProjectPO();
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        projectPO.setOnCall(true);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));

        return OnCallRule.builder().project(Project.builder().id(1L).build()).fromMail("@dtime.se").build();
    }

}