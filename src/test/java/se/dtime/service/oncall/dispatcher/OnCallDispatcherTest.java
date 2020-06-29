package se.dtime.service.oncall.dispatcher;

import org.junit.Test;
import se.dtime.dbmodel.oncall.OnCallRulePO;
import se.dtime.model.EmailContainer;

import java.util.List;

import static org.junit.Assert.*;

public class OnCallDispatcherTest {
    private OnCallDispatcher onCallDispatcher = new OnCallDispatcher();

    @Test
    public void isMatchEmailSubjectAndBodyMatch() {
        EmailContainer emailContainer = createEmailContainer();
        assertTrue(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, true)));
    }

    @Test
    public void isMatchEmailNotMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setFrom("test@gmail.com");
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, true)));
    }

    @Test
    public void isMatchSubjectNotMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setSubject("hej hopp");
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, true)));
    }

    @Test
    public void isMatchBodyNotMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setBody("hej hopp");
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, true)));
    }

    @Test
    public void isMatchOnlySubjectRuleAndSubjectMatch() {
        EmailContainer emailContainer = createEmailContainer();
        assertTrue(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, false)));
    }

    @Test
    public void isMatchOnlySubjectRuleAndSubjectNotMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setSubject("test");
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, false)));
    }

    @Test
    public void isMatchOnlyBodyRuleAndBodyMatch() {
        EmailContainer emailContainer = createEmailContainer();
        assertTrue(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(false, true)));
    }

    @Test
    public void isMatchOnlyBodyRuleAndBodyNotMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setBody("test");
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(false, true)));
    }

    @Test
    public void isMatchNoSubject() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setSubject(null);
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, true)));
    }

    @Test
    public void isMatchNoBody() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setBody(null);
        assertFalse(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, true)));
    }

    @Test
    public void isMatchNoSubjectAndBodyMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setSubject(null);
        assertTrue(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(false, true)));
    }

    @Test
    public void isMatchNoBodyAndSubjectMatch() {
        EmailContainer emailContainer = createEmailContainer();
        emailContainer.setBody(null);
        assertTrue(onCallDispatcher.isMatch(emailContainer, createCallRulePOS(true, false)));
    }

    @Test
    public void testCSVSSplit() {
        OnCallRulePO onCallRulePOS = createCallRulePOS(true, true);

        List<String> subjects = onCallRulePOS.getSubjectCSVAsArray();
        assertEquals("CRITICAL", subjects.get(0));
        assertEquals("ERROR", subjects.get(1));
        assertEquals("WARN", subjects.get(2));

        List<String> bodies = onCallRulePOS.getBodyCSVAsArray();
        assertEquals("XYZ", bodies.get(0));
        assertEquals("crash", bodies.get(1));
        assertEquals("Emergency", bodies.get(2));

    }

    private EmailContainer createEmailContainer() {
        return EmailContainer.builder().from("info@dtime.se").
                subject("System status CRITICAL").body("Mail sender computer XYZ. System crash. Emergency").build();
    }

    private OnCallRulePO createCallRulePOS(boolean subjectRule, boolean bodyRule)  {
        OnCallRulePO onCallRulePO = new OnCallRulePO();
        onCallRulePO.setFromEmail("@dtime.se");
        onCallRulePO.setSubjectCSV(subjectRule ? "  CRITICAL   , ERROR  , WARN  " : null);
        onCallRulePO.setBodyCSV(bodyRule ? "XYZ ,crash, Emergency" : null);
        return onCallRulePO;
    }
}