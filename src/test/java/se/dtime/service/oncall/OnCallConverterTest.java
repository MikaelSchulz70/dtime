package se.dtime.service.oncall;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.*;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.oncall.OnCallConfigPO;
import se.dtime.dbmodel.oncall.OnCallPO;
import se.dtime.model.oncall.OnCallDayConfig;
import se.dtime.model.oncall.OnCallProject;
import se.dtime.model.oncall.OnCallProjectConfig;
import se.dtime.model.oncall.OnCallUser;
import se.dtime.model.timereport.Day;
import se.dtime.service.project.ProjectConverter;
import se.dtime.service.user.UserConverter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class OnCallConverterTest {
    @InjectMocks
    private OnCallConverter onCallConverter;
    @Mock
    private UserConverter userConverter;
    @Mock
    private ProjectConverter projectConverter;


    @Test
    public void toOnCallProjectsAdmin() {
        List<ProjectPO> projectPOS = createProjects();
        List<AssignmentPO> assignmentPOS = createAssignments();
        List<OnCallPO> onCallPOS = createOnCallPOS();
        boolean isAdmin = true;
        Day[] days = createDays();

        List<OnCallProject> onCallProjects = onCallConverter.toOnCallProjects(projectPOS, assignmentPOS, onCallPOS, isAdmin, days);

        assertEquals(3, onCallProjects.size());

        // Project 1
        OnCallProject onCallProject1 = onCallProjects.get(0);
        assertEquals(1, onCallProject1.getIdProject());
        assertEquals("Company 1", onCallProject1.getCompanyName());
        assertEquals("Project 1", onCallProject1.getProjectName());
        assertEquals(2, onCallProject1.getOnCallUsers().size());

        OnCallUser onCallUser1 = onCallProject1.getOnCallUsers().get(0);
        assertEquals(10, onCallUser1.getIdUser());
        assertEquals(3, onCallUser1.getOnCallDays().size());
        assertEquals(1, onCallUser1.getOnCallDays().get(0).getIdAssignment());
        assertTrue(onCallUser1.getOnCallDays().get(0).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(1).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(2).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(0).isReadOnly());
        assertFalse(onCallUser1.getOnCallDays().get(1).isReadOnly());
        assertFalse(onCallUser1.getOnCallDays().get(2).isReadOnly());

        OnCallUser onCallUser2 = onCallProject1.getOnCallUsers().get(1);
        assertEquals(11, onCallUser2.getIdUser());
        assertEquals(3, onCallUser2.getOnCallDays().size());
        assertEquals(2, onCallUser2.getOnCallDays().get(0).getIdAssignment());
        assertFalse(onCallUser2.getOnCallDays().get(0).isOnCall());
        assertFalse(onCallUser2.getOnCallDays().get(1).isOnCall());
        assertFalse(onCallUser2.getOnCallDays().get(2).isOnCall());
        assertFalse(onCallUser2.getOnCallDays().get(0).isReadOnly());
        assertFalse(onCallUser2.getOnCallDays().get(1).isReadOnly());
        assertFalse(onCallUser2.getOnCallDays().get(2).isReadOnly());

        // Project 2
        OnCallProject onCallProject2 = onCallProjects.get(1);
        assertEquals(1, onCallProject1.getIdProject());
        assertEquals("Company 1", onCallProject2.getCompanyName());
        assertEquals("Project 2", onCallProject2.getProjectName());
        assertEquals(1, onCallProject2.getOnCallUsers().size());
        onCallUser1 = onCallProject2.getOnCallUsers().get(0);
        assertEquals(3, onCallUser1.getOnCallDays().size());
        assertEquals(3, onCallUser1.getOnCallDays().get(0).getIdAssignment());
        assertFalse(onCallUser1.getOnCallDays().get(0).isOnCall());
        assertTrue(onCallUser1.getOnCallDays().get(1).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(2).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(0).isReadOnly());
        assertFalse(onCallUser1.getOnCallDays().get(1).isReadOnly());
        assertFalse(onCallUser1.getOnCallDays().get(2).isReadOnly());


        OnCallUser onCallUser3 =  onCallProject1.getOnCallUsers().get(0);
        assertEquals(10, onCallUser3.getIdUser());
        assertEquals(3, onCallUser3.getOnCallDays().size());

        // Project 3
        OnCallProject onCallProject3 = onCallProjects.get(2);
        assertEquals("Company 2", onCallProject3.getCompanyName());
        assertEquals("Project 3", onCallProject3.getProjectName());
        assertEquals(0, onCallProject3.getOnCallUsers().size());
    }

    @Test
    public void toOnCallProjectsUser() {
        List<ProjectPO> projectPOS = createProjects();
        List<AssignmentPO> assignmentPOS = createAssignments();
        List<OnCallPO> onCallPOS = createOnCallPOS();
        boolean isAdmin = false;
        Day[] days = createDays();

        List<OnCallProject> onCallProjects = onCallConverter.toOnCallProjects(projectPOS, assignmentPOS, onCallPOS, isAdmin, days);

        assertEquals(3, onCallProjects.size());

        // Project 1
        OnCallProject onCallProject1 = onCallProjects.get(0);
        assertEquals(1, onCallProject1.getIdProject());
        assertEquals("Company 1", onCallProject1.getCompanyName());
        assertEquals("Project 1", onCallProject1.getProjectName());
        assertEquals(2, onCallProject1.getOnCallUsers().size());

        OnCallUser onCallUser1 = onCallProject1.getOnCallUsers().get(0);
        assertEquals(10, onCallUser1.getIdUser());
        assertEquals(3, onCallUser1.getOnCallDays().size());
        assertEquals(1, onCallUser1.getOnCallDays().get(0).getIdAssignment());
        assertTrue(onCallUser1.getOnCallDays().get(0).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(1).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(2).isOnCall());
        assertTrue(onCallUser1.getOnCallDays().get(0).isReadOnly());
        assertTrue(onCallUser1.getOnCallDays().get(1).isReadOnly());
        assertTrue(onCallUser1.getOnCallDays().get(2).isReadOnly());

        OnCallUser onCallUser2 = onCallProject1.getOnCallUsers().get(1);
        assertEquals(11, onCallUser2.getIdUser());
        assertEquals(3, onCallUser2.getOnCallDays().size());
        assertEquals(2, onCallUser2.getOnCallDays().get(0).getIdAssignment());
        assertFalse(onCallUser2.getOnCallDays().get(0).isOnCall());
        assertFalse(onCallUser2.getOnCallDays().get(1).isOnCall());
        assertFalse(onCallUser2.getOnCallDays().get(2).isOnCall());
        assertTrue(onCallUser2.getOnCallDays().get(0).isReadOnly());
        assertTrue(onCallUser2.getOnCallDays().get(1).isReadOnly());
        assertTrue(onCallUser2.getOnCallDays().get(2).isReadOnly());

        // Project 2
        OnCallProject onCallProject2 = onCallProjects.get(1);
        assertEquals(1, onCallProject1.getIdProject());
        assertEquals("Company 1", onCallProject2.getCompanyName());
        assertEquals("Project 2", onCallProject2.getProjectName());
        assertEquals(1, onCallProject2.getOnCallUsers().size());
        onCallUser1 = onCallProject2.getOnCallUsers().get(0);
        assertEquals(3, onCallUser1.getOnCallDays().size());
        assertEquals(3, onCallUser1.getOnCallDays().get(0).getIdAssignment());
        assertFalse(onCallUser1.getOnCallDays().get(0).isOnCall());
        assertTrue(onCallUser1.getOnCallDays().get(1).isOnCall());
        assertFalse(onCallUser1.getOnCallDays().get(2).isOnCall());
        assertTrue(onCallUser1.getOnCallDays().get(0).isReadOnly());
        assertTrue(onCallUser1.getOnCallDays().get(1).isReadOnly());
        assertTrue(onCallUser1.getOnCallDays().get(2).isReadOnly());

        OnCallUser onCallUser3 =  onCallProject1.getOnCallUsers().get(0);
        assertEquals(10, onCallUser3.getIdUser());
        assertEquals(3, onCallUser3.getOnCallDays().size());

        // Project 3
        OnCallProject onCallProject3 = onCallProjects.get(2);
        assertEquals("Company 2", onCallProject3.getCompanyName());
        assertEquals("Project 3", onCallProject3.getProjectName());
        assertEquals(0, onCallProject3.getOnCallUsers().size());
    }

    @Test
    public void toOnCallProjectConfigsAdminTest() {
        List<ProjectPO> projectPOS = createProjects();
        List<OnCallConfigPO> onCallConfigPOS = createOnCallConfigPOS();
        boolean isAdmin = true;

        List<OnCallProjectConfig> onCallProjectConfigs = onCallConverter.toOnCallProjectConfigs(projectPOS, onCallConfigPOS, isAdmin);
        assertEquals(3, onCallProjectConfigs.size());

        OnCallProjectConfig onCallProjectConfig1 = onCallProjectConfigs.get(0);
        assertOk(onCallProjectConfig1, DayOfWeek.MONDAY, LocalTime.of(9,  0), LocalTime.of(10, 0), false);

        OnCallProjectConfig onCallProjectConfig2 = onCallProjectConfigs.get(1);
        assertOk(onCallProjectConfig2, DayOfWeek.TUESDAY, LocalTime.of(11,  15), LocalTime.of(17, 15), false);

        OnCallProjectConfig onCallProjectConfig3 = onCallProjectConfigs.get(2);
        assertOk(onCallProjectConfig3, DayOfWeek.WEDNESDAY, LocalTime.of(1,  15), LocalTime.of(23, 59), false);
    }

    @Test
    public void toOnCallProjectConfigsUserTest() {
        List<ProjectPO> projectPOS = createProjects();
        List<OnCallConfigPO> onCallConfigPOS = createOnCallConfigPOS();
        boolean isAdmin = false;

        List<OnCallProjectConfig> onCallProjectConfigs = onCallConverter.toOnCallProjectConfigs(projectPOS, onCallConfigPOS, isAdmin);
        assertEquals(3, onCallProjectConfigs.size());

        OnCallProjectConfig onCallProjectConfig1 = onCallProjectConfigs.get(0);
        assertOk(onCallProjectConfig1, DayOfWeek.MONDAY, LocalTime.of(9,  0), LocalTime.of(10, 0), true);

        OnCallProjectConfig onCallProjectConfig2 = onCallProjectConfigs.get(1);
        assertOk(onCallProjectConfig2, DayOfWeek.TUESDAY, LocalTime.of(11,  15), LocalTime.of(17, 15), true);

        OnCallProjectConfig onCallProjectConfig3 = onCallProjectConfigs.get(2);
        assertOk(onCallProjectConfig3, DayOfWeek.WEDNESDAY, LocalTime.of(1,  15), LocalTime.of(23, 59), true);
    }

    private void assertOk(OnCallProjectConfig onCallProjectConfig, DayOfWeek day, LocalTime startTime, LocalTime endTime, boolean isReadOnly) {
        assertEquals(7, onCallProjectConfig.getOnCallDayConfigs().size());
        for (OnCallDayConfig onCallDayConfig : onCallProjectConfig.getOnCallDayConfigs()) {
            if (onCallDayConfig.getDayOfWeek() == day) {
                assertEquals(startTime, onCallDayConfig.getStartTime());
                assertEquals(endTime, onCallDayConfig.getEndTime());
                assertEquals(isReadOnly, onCallDayConfig.isReadOnly());
            } else {
                assertNull(onCallDayConfig.getStartTime());
                assertNull(onCallDayConfig.getEndTime());
                assertEquals(isReadOnly, onCallDayConfig.isReadOnly());
            }
        }
    }

    private List<AssignmentPO> createAssignments() {
        List<AssignmentPO> assignmentPOS = new ArrayList<>();

        AssignmentPO assignmentPO1 = new AssignmentPO(1);
        assignmentPO1.setProject(new ProjectPO(1L));
        assignmentPO1.setUser(new UserPO(10L));

        AssignmentPO assignmentPO2 = new AssignmentPO(2);
        assignmentPO2.setProject(new ProjectPO(1L));
        assignmentPO2.setUser(new UserPO(11L));

        AssignmentPO assignmentPO3 = new AssignmentPO(3);
        assignmentPO3.setProject(new ProjectPO(2L));
        assignmentPO3.setUser(new UserPO(10L));

        assignmentPOS.add(assignmentPO1);
        assignmentPOS.add(assignmentPO2);
        assignmentPOS.add(assignmentPO3);

        return assignmentPOS;
    }

    private List<ProjectPO> createProjects() {
        List<ProjectPO> projectPOS = new ArrayList<>();
        ProjectPO projectPO1 = new ProjectPO(1L);
        projectPO1.setName("Project 1");
        CompanyPO companyPO1 = new CompanyPO(1L);
        companyPO1.setName("Company 1");
        projectPO1.setCompany(companyPO1);

        ProjectPO projectPO2 = new ProjectPO(2L);
        projectPO2.setName("Project 2");
        projectPO2.setCompany(companyPO1);

        ProjectPO projectPO3 = new ProjectPO(3L);
        projectPO3.setName("Project 3");
        CompanyPO companyPO2 = new CompanyPO(2L);
        companyPO2.setName("Company 2");
        projectPO3.setCompany(companyPO2);

        projectPOS.add(projectPO1);
        projectPOS.add(projectPO2);
        projectPOS.add(projectPO3);

        return projectPOS;
    }

    private List<OnCallConfigPO> createOnCallConfigPOS() {
        List<OnCallConfigPO> onCallConfigPOS = new ArrayList<>();

        OnCallConfigPO onCallConfigPO1 = new OnCallConfigPO();
        onCallConfigPO1.setProject(new ProjectPO(1L));
        onCallConfigPO1.setDayOfWeek(DayOfWeek.MONDAY);
        onCallConfigPO1.setStartTime(LocalTime.of(9, 0));
        onCallConfigPO1.setEndTime(LocalTime.of(10, 0));
        onCallConfigPOS.add(onCallConfigPO1);

        OnCallConfigPO onCallConfigPO2 = new OnCallConfigPO();
        onCallConfigPO2.setProject(new ProjectPO(2L));
        onCallConfigPO2.setStartTime(LocalTime.of(11, 15));
        onCallConfigPO2.setEndTime(LocalTime.of(17, 15));
        onCallConfigPO2.setDayOfWeek(DayOfWeek.TUESDAY);
        onCallConfigPOS.add(onCallConfigPO2);

        OnCallConfigPO onCallConfigPO3 = new OnCallConfigPO();
        onCallConfigPO3.setProject(new ProjectPO(3L));
        onCallConfigPO3.setStartTime(LocalTime.of(1, 15));
        onCallConfigPO3.setEndTime(LocalTime.of(23, 59));
        onCallConfigPO3.setDayOfWeek(DayOfWeek.WEDNESDAY);
        onCallConfigPOS.add(onCallConfigPO3);

        return onCallConfigPOS;
    }

    private List<OnCallPO> createOnCallPOS() {
        List<OnCallPO> onCallPOS = new ArrayList<>();

        LocalDate now = LocalDate.now();

        OnCallPO onCallPO1 = new OnCallPO(1);
        onCallPO1.setDate(now);
        AssignmentPO assignmentPO1 = new AssignmentPO(1);
        assignmentPO1.setProject(new ProjectPO(1L));
        assignmentPO1.setUser(new UserPO(10L));
        onCallPO1.setAssignment(assignmentPO1);

        OnCallPO onCallPO2 = new OnCallPO(2);
        onCallPO2.setDate(now.plusDays(1));
        AssignmentPO assignmentPO2 = new AssignmentPO(3);
        assignmentPO2.setProject(new ProjectPO(2L));
        assignmentPO2.setUser(new UserPO(10L));
        onCallPO2.setAssignment(assignmentPO2);

        onCallPOS.add(onCallPO1);
        onCallPOS.add(onCallPO2);

        return onCallPOS;
    }

    private Day[] createDays() {
        Day[] days = new Day[3];

        LocalDate now = LocalDate.now();
        days[0] = Day.builder().date(now).build();
        days[1] = Day.builder().date(now.plusDays(1)).build();
        days[2] = Day.builder().date(now.plusDays(2)).build();

        return days;
    }
}