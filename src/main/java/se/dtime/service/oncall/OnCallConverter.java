package se.dtime.service.oncall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.oncall.*;
import se.dtime.model.ActivationStatus;
import se.dtime.model.oncall.*;
import se.dtime.model.timereport.Day;
import se.dtime.service.BaseConverter;
import se.dtime.service.project.ProjectConverter;
import se.dtime.utils.DateTimeUtil;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OnCallConverter extends BaseConverter {
    @Autowired
    private ProjectConverter projectConverter;

    public OnCallPO toPO(OnCallDay onCallDay) {
        if (onCallDay == null) {
            return null;
        }

        OnCallPO onCallPO = new OnCallPO();
        onCallPO.setId(onCallDay.getId());
        onCallPO.setAssignment(new AssignmentPO(onCallDay.getIdAssignment()));
        onCallPO.setDate(onCallDay.getDay().getDate());
        updateBaseData(onCallPO);

        return onCallPO;
    }

    public List<OnCallProject> toOnCallProjects(List<ProjectPO> projectPOS,
                                                List<AssignmentPO> assignmentPOS,
                                                List<OnCallPO> onCallPOS,
                                                boolean isAdmin,
                                                Day[] days) {
        List<OnCallProject> onCallProjects = projectPOS.stream().map(p -> toOnCallProject(p, assignmentPOS, onCallPOS, isAdmin, days)).collect(Collectors.toList());

        onCallProjects.sort((ocp1, ocp2) -> ocp1.getCompanyName().compareTo(ocp2.getCompanyName()));

        return onCallProjects;
    }

    private OnCallProject toOnCallProject(ProjectPO projectPO,
                                          List<AssignmentPO> assignmentPOS,
                                          List<OnCallPO> onCallPOS,
                                          boolean isAdmin,
                                          Day[] days) {

        List<AssignmentPO> assignmentsForProject = assignmentPOS.stream().filter(a -> a.getProject().getId().equals(projectPO.getId())).collect(Collectors.toList());
        List<OnCallPO> onCallsForProject = onCallPOS.stream().filter(oc -> oc.getAssignment().getProject().getId().equals(projectPO.getId())).collect(Collectors.toList());

        List<OnCallUser> onCallUsers = toCallUsers(assignmentsForProject, onCallsForProject, days, isAdmin);
        return OnCallProject.builder().
                idProject(projectPO.getId()).
                projectName(projectPO.getName()).
                companyName(projectPO.getCompany().getName()).
                onCallUsers(onCallUsers).
                build();
    }

    private List<OnCallUser> toCallUsers(List<AssignmentPO> assignmentPOS, List<OnCallPO> onCallPOS, Day[] days, boolean isAdmin) {
        List<OnCallUser> onCallUsers = new ArrayList<>();
        Set<UserPO> userPOS = assignmentPOS.stream().map(AssignmentPO::getUser).collect(Collectors.toSet());

        for (AssignmentPO assignmentPO : assignmentPOS) {
            OnCallUser onCallUser = OnCallUser.builder().
                    idUser(assignmentPO.getUser().getId()).
                    userName(assignmentPO.getUser().getFullName()).
                    onCallDays(new ArrayList<>()).build();
            onCallUsers.add(onCallUser);

            for (Day day : days) {
                OnCallDay onCallDay = findOrCreateOnCallDay(day, assignmentPO, onCallPOS, isAdmin);
                onCallUser.getOnCallDays().add(onCallDay);
            }
        }

        return onCallUsers;
    }

    private OnCallDay findOrCreateOnCallDay(Day day, AssignmentPO assignmentPO, List<OnCallPO> onCallPOS, boolean isAdmin) {
        return onCallPOS.stream().
                filter(oc -> oc.getDate().equals(day.getDate()) && oc.getAssignment().getUser().getId().equals(assignmentPO.getUser().getId())).
                findFirst().
                map(oc -> OnCallDay.builder().id(oc.getId()).idAssignment(assignmentPO.getId()).day(day).isOnCall(true).isReadOnly(!isAdmin).build()).
                orElse(OnCallDay.builder().idAssignment(assignmentPO.getId()).day(day).isOnCall(false).isReadOnly(!isAdmin).build());
    }

    public List<OnCallProjectConfig> toOnCallProjectConfigs(List<ProjectPO> projectPOS, List<OnCallConfigPO> onCallConfigPOS, boolean isAdmin) {
        List<OnCallProjectConfig> onCallProjectConfigs = new ArrayList<>();

        for (ProjectPO projectPO : projectPOS) {
            List<OnCallDayConfig> onCallDayConfigs = toOnCallDayConfigs(projectPO, onCallConfigPOS, isAdmin);

            OnCallProjectConfig onCallProjectConfig = OnCallProjectConfig.
                    builder().
                    idProject(projectPO.getId()).
                    projectName(projectPO.getName()).
                    companyName(projectPO.getCompany().getName()).
                    onCallDayConfigs(onCallDayConfigs).
                    build();

            onCallProjectConfigs.add(onCallProjectConfig);
        }

        return onCallProjectConfigs;
    }

    private List<OnCallDayConfig> toOnCallDayConfigs(ProjectPO projectPO, List<OnCallConfigPO> onCallConfigPOS, boolean isAdmin) {
        List<OnCallDayConfig> onCallDayConfigs = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            OnCallDayConfig onCallDayConfig = findOrCreateOnCallDayConfig(projectPO.getId(), dayOfWeek, onCallConfigPOS, isAdmin);
            onCallDayConfigs.add(onCallDayConfig);
        }
        return onCallDayConfigs;
    }

    private OnCallDayConfig findOrCreateOnCallDayConfig(long idProject, DayOfWeek dayOfWeek, List<OnCallConfigPO> onCallConfigPOS, boolean isAdmin) {
        return onCallConfigPOS.stream().
                filter(c -> c.getProject().getId() == idProject && c.getDayOfWeek() == dayOfWeek).
                findFirst().
                map(c -> OnCallDayConfig.builder().idProject(idProject).dayOfWeek(dayOfWeek).startTime(c.getStartTime()).endTime(c.getEndTime()).isReadOnly(!isAdmin).build()).
                orElse(OnCallDayConfig.builder().idProject(idProject).dayOfWeek(dayOfWeek).isReadOnly(!isAdmin).build());
    }

    public OnCallConfigPO toPO(OnCallDayConfig onCallDayConfig) {
        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setEndTime(onCallDayConfig.getEndTime());
        onCallConfigPO.setStartTime(onCallDayConfig.getStartTime());
        onCallConfigPO.setActivationStatus(ActivationStatus.ACTIVE);
        onCallConfigPO.setDayOfWeek(onCallDayConfig.getDayOfWeek());
        onCallConfigPO.setProject(new ProjectPO(onCallDayConfig.getIdProject()));
        updateBaseData(onCallConfigPO);
        return onCallConfigPO;
    }

    public OnCallRulePO toPO(OnCallRule onCallRule) {
        OnCallRulePO onCallRulePO = new OnCallRulePO();
        onCallRulePO.setId(onCallRule.getId());
        onCallRulePO.setProject(new ProjectPO(onCallRule.getProject().getId()));
        onCallRulePO.setFromEmail(onCallRule.getFromMail());
        onCallRulePO.setSubjectCSV(onCallRule.getSubjectCSV());
        onCallRulePO.setBodyCSV(onCallRule.getBodyCSV());
        super.updateBaseData(onCallRulePO);
        return onCallRulePO;
    }

    public OnCallRule toOnCallRuleModel(OnCallRulePO onCallRulePO) {
        return OnCallRule.builder().
                id(onCallRulePO.getId()).
                fromMail(onCallRulePO.getFromEmail()).
                subjectCSV(onCallRulePO.getSubjectCSV()).
                bodyCSV(onCallRulePO.getBodyCSV()).
                project(projectConverter.toModel(onCallRulePO.getProject())).
                build();
    }

    public List<OnCallRule> toOnCallRuleModel(List<OnCallRulePO> onCallRulePOS) {
        return onCallRulePOS.stream().map(this::toOnCallRuleModel).collect(Collectors.toList());
    }

    public OnCallSession toModel(OnCallSessionPO onCallSessionPO) {
        return OnCallSession.
                builder().
                id(onCallSessionPO.getId()).
                lastPollDateTime(DateTimeUtil.formateDateTime(onCallSessionPO.getLastPollDateTime())).
                totalEmails(onCallSessionPO.getTotalEmails()).
                totalDispatched(onCallSessionPO.getTotalDispatched()).
                dispatchedInLastPoll(onCallSessionPO.getDispatchedInLastPoll()).
                mailInInboxInLastPoll(onCallSessionPO.getMailInInboxInLastPoll()).
                readInLastPoll(onCallSessionPO.getReadInLastPoll()).
                message(onCallSessionPO.getMessage()).
                build();
    }

    public List<OnCallAlarm> toPOs(List<OnCallAlarmPO> onCallAlarmPOS) {
        return onCallAlarmPOS.stream().map(a -> toPO(a)).collect(Collectors.toList());
    }

    public OnCallAlarm toPO(OnCallAlarmPO onCallAlarmPO) {
        String userName = (onCallAlarmPO.getUser() != null ? onCallAlarmPO.getUser().getFullName() : "System System");
        String projectName = (onCallAlarmPO.getProject() != null ? onCallAlarmPO.getProject().getName() : "");
        String companyName = (onCallAlarmPO.getProject() != null && onCallAlarmPO.getProject().getCompany() != null ?
                onCallAlarmPO.getProject().getCompany().getName() : "");

        return OnCallAlarm.
                builder().
                dateTime(DateTimeUtil.formateDateTime(onCallAlarmPO.getCreateDateTime())).
                emailSent(onCallAlarmPO.isEmailSent()).
                id(onCallAlarmPO.getId()).
                sender(onCallAlarmPO.getSender()).
                subject(onCallAlarmPO.getSubject()).
                instantMsgSent(onCallAlarmPO.isInstantMsgSent()).
                message(onCallAlarmPO.getMessage()).
                status(onCallAlarmPO.getStatus()).
                onCallSeverity(onCallAlarmPO.getOnCallSeverity()).
                userName(userName).
                projectName(projectName).
                companyName(companyName).
                build();
    }
}
