package se.dtime.service.oncall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallConfigPO;
import se.dtime.dbmodel.oncall.OnCallPO;
import se.dtime.dbmodel.oncall.OnCallRulePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserExt;
import se.dtime.model.error.ValidationException;
import se.dtime.model.oncall.*;
import se.dtime.model.timereport.Day;
import se.dtime.repository.*;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.project.ProjectConverter;
import se.dtime.service.system.SystemService;
import se.dtime.service.user.UserValidator;
import se.dtime.utils.UserUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OnCallService {
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private OnCallValidator onCallValidator;
    @Autowired
    private OnCallRepository onCallRepository;
    @Autowired
    private OnCallConfigRepository onCallConfigRepository;
    @Autowired
    private OnCallConverter onCallConverter;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private OnCallRuleRepository onCallRuleRepository;
    @Autowired
    private ProjectConverter projectConverter;
    @Autowired
    private OnCallRuleValidator onCallRuleValidator;
    @Autowired
    private SystemService systemService;

    @Value("${sms.phonenumber}")
    private String onCallMobileNumber;

    public void addOrUpdate(OnCallDay onCallDay) {
        userValidator.validateLoggedIn();
        onCallValidator.validateAdd(onCallDay);

        OnCallPO onCallPO = onCallRepository.findByAssignmentAndDate(onCallDay.getIdAssignment(), onCallDay.getDay().getDate());
        if (onCallDay.isOnCall()) {
            if (onCallPO == null) {
                onCallRepository.save(onCallConverter.toPO(onCallDay));
            }
        } else {
            if (onCallPO != null) {
                onCallRepository.delete(onCallPO);
            }
        }
    }

    public OnCallReport getCurrentOnCallReport() {
        return getOnCallReport(calendarService.getNowDate());
    }

    public OnCallReport getPreviousOnCallReport(LocalDate date) {
        return getOnCallReport(date.minusMonths(1));
    }

    public OnCallReport getNextOnCallReport(LocalDate date) {
        return getOnCallReport(date.plusMonths(1));
    }

    private OnCallReport getOnCallReport(LocalDate date) {
        LocalDate fromDate = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
        LocalDate toDate = LocalDate.of(date.getYear(), date.getMonthValue(), fromDate.lengthOfMonth());
        Day[] days = calendarService.getDays(fromDate, toDate);

        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        ;

        List<AssignmentPO> assignmentPOS = assignmentRepository.findByProjectOnCallTrueAndActivationStatus(ActivationStatus.ACTIVE);
        List<OnCallPO> onCallPOS = onCallRepository.findByBetweenDates(fromDate, toDate);

        List<ProjectPO> projectPOS;
        if (isAdmin) {
            projectPOS = projectRepository.findByOnCallTrueAndActivationStatus(ActivationStatus.ACTIVE);
        } else {
            assignmentPOS = assignmentPOS.stream().filter(a -> a.getUser().getId() == userExt.getId()).collect(Collectors.toList());
            projectPOS = assignmentPOS.stream().map(AssignmentPO::getProject).collect(Collectors.toList());
            onCallPOS = onCallPOS.stream().filter(oc -> oc.getAssignment().getUser().getId() == userExt.getId()).collect(Collectors.toList());
        }

        List<OnCallProject> onCallProjects = onCallConverter.toOnCallProjects(projectPOS, assignmentPOS, onCallPOS, isAdmin, days);

        return OnCallReport.builder().days(days).isReadOnly(!isAdmin).onCallProjects(onCallProjects).build();
    }

    public OnCallConfig getOnCallConfig() {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        ;

        List<OnCallConfigPO> onCallConfigPOS = onCallConfigRepository.findAll();

        List<ProjectPO> projectPOS;
        if (isAdmin) {
            projectPOS = projectRepository.findByOnCallTrueAndActivationStatus(ActivationStatus.ACTIVE);
        } else {
            List<AssignmentPO> assignmentPOS = assignmentRepository.findByProjectOnCallTrueAndActivationStatus(ActivationStatus.ACTIVE);
            assignmentPOS = assignmentPOS.stream().filter(a -> a.getUser().getId() == userExt.getId()).collect(Collectors.toList());
            projectPOS = assignmentPOS.stream().map(AssignmentPO::getProject).collect(Collectors.toList());
        }

        List<OnCallProjectConfig> onCallProjectConfigs = onCallConverter.toOnCallProjectConfigs(projectPOS, onCallConfigPOS, isAdmin);

        return OnCallConfig.
                builder().
                onCallProjectConfigs(onCallProjectConfigs).
                onCallPhoneNumber(onCallMobileNumber).
                readOnly(!isAdmin).
                build();
    }

    public void addOrUpdate(OnCallDayConfig onCallDayConfig) {
        onCallValidator.validate(onCallDayConfig);

        OnCallConfigPO onCallConfigPO = onCallConfigRepository.findByProjectIdAndDayOfWeek(onCallDayConfig.getIdProject(), onCallDayConfig.getDayOfWeek());
        if (onCallConfigPO != null) {
            onCallConfigPO.setStartTime(onCallDayConfig.getStartTime());
            onCallConfigPO.setEndTime(onCallDayConfig.getEndTime());
            onCallConverter.updateBaseData(onCallConfigPO);
        } else {
            onCallConfigPO = onCallConverter.toPO(onCallDayConfig);
        }

        onCallConfigRepository.save(onCallConfigPO);
    }

    public long addOrUpdate(OnCallRule onCallRule) {
        onCallRuleValidator.validateAdd(onCallRule);
        OnCallRulePO onCallRulePO = onCallConverter.toPO(onCallRule);
        onCallRuleRepository.save(onCallRulePO);
        return onCallRulePO.getId();
    }

    public List<OnCallRule> getOnCallRules() {
        List<OnCallRulePO> onCallRulePOS = onCallRuleRepository.findAll();
        List<ProjectPO> projectPOS = projectRepository.findByOnCallTrueAndActivationStatus(ActivationStatus.ACTIVE);

        List<OnCallRule> onCallRules = new ArrayList<>();
        for (ProjectPO projectPO : projectPOS) {
            OnCallRule onCallRule = onCallRulePOS.stream().
                    filter(oc -> projectPO.getId().equals(oc.getProject().getId())).
                    findFirst().
                    map(oc -> onCallConverter.toOnCallRuleModel(oc)).
                    orElse(OnCallRule.builder().project(projectConverter.toModel(projectPO)).build());
            onCallRules.add(onCallRule);
        }

        return onCallRules;
    }

    public OnCallRule getOnCallRule(long projectId) {
        List<OnCallRulePO> onCallRules = onCallRuleRepository.findByProject(new ProjectPO(projectId));
        ProjectPO projectPO = null;
        if (onCallRules.size() == 0) {
            projectPO = projectRepository.findById(projectId).orElseThrow(() -> new ValidationException("project.not.found"));
        }

        return onCallRules.stream().
                findFirst().
                map(r -> onCallConverter.toOnCallRuleModel(r)).
                orElse(OnCallRule.builder().project(projectConverter.toModel(projectPO)).build());
    }

    public List<OnCallProject> getOnCallProjects(LocalDateTime dateTime) {
        List<OnCallConfigPO> onCallConfigPOS = onCallConfigRepository.findByDayOfWeekAndActivationStatus(dateTime.getDayOfWeek(), ActivationStatus.ACTIVE);

        onCallConfigPOS = onCallConfigPOS.stream().filter(occ -> isWithinOnCallTime(occ, dateTime.toLocalTime())).collect(Collectors.toList());

        List<OnCallPO> onCallPOS = new ArrayList<>();
        onCallConfigPOS.forEach(occ -> onCallPOS.addAll(onCallRepository.findByProjectAndDate(occ.getProject().getId(), dateTime.toLocalDate())));

        Map<Long, OnCallProject> onCallProjectMap = new HashMap<>();
        for (OnCallPO onCallPO : onCallPOS) {
            OnCallProject onCallProject = onCallProjectMap.get(onCallPO.getAssignment().getProject().getId());
            if (onCallProject == null) {
                onCallProject = OnCallProject.builder().
                        idProject(onCallPO.getAssignment().getProject().getId()).
                        projectName(onCallPO.getAssignment().getProject().getName()).
                        companyName(onCallPO.getAssignment().getProject().getCompany().getName()).
                        onCallUsers(new ArrayList<>()).
                        build();
                onCallProjectMap.put(onCallPO.getAssignment().getProject().getId(), onCallProject);
            }

            OnCallUser onCallUser = OnCallUser.builder().idUser(onCallPO.getAssignment().getUser().getId()).
                    userName(onCallPO.getAssignment().getUser().getFullName()).
                    email(onCallPO.getAssignment().getUser().getEmail()).
                    mobileNumber(onCallPO.getAssignment().getUser().getMobileNumber()).
                    build();

            onCallProject.getOnCallUsers().add(onCallUser);
        }

        return new ArrayList<>(onCallProjectMap.values());
    }

    public List<UserOnCall> getUsersOnCall(long idProject) {
        LocalDateTime now = calendarService.getNowDateTime();

        OnCallConfigPO onCallConfigPO = onCallConfigRepository.findByProjectIdAndDayOfWeek(idProject, now.getDayOfWeek());
        if (!isWithinOnCallTime(onCallConfigPO, now.toLocalTime())) {
            return new ArrayList<>();
        }

        List<OnCallPO> onCallPOS = onCallRepository.findByProjectAndDate(idProject, now.toLocalDate());
        if (onCallPOS.isEmpty()) {
            return new ArrayList<>();
        }

        return onCallPOS.
                stream().
                map(a -> UserOnCall.builder().
                        email(a.getAssignment().getUser().getEmail()).
                        name(a.getAssignment().getUser().getFullName()).
                        mobileNumber(a.getAssignment().getUser().getMobileNumber()).
                        build()).
                collect(Collectors.toList());
    }

    boolean isWithinOnCallTime(OnCallConfigPO onCallConfigPO, LocalTime now) {
        if (onCallConfigPO == null || (onCallConfigPO.getStartTime() == null && onCallConfigPO.getEndTime() == null)) {
            return false;
        }

        if (onCallConfigPO.getStartTime() != null && onCallConfigPO.getEndTime() == null) {
            return now.equals(onCallConfigPO.getStartTime()) || now.isAfter(onCallConfigPO.getStartTime());
        }

        if (onCallConfigPO.getStartTime() == null && onCallConfigPO.getEndTime() != null) {
            return now.equals(onCallConfigPO.getEndTime()) || now.isBefore(onCallConfigPO.getEndTime());
        }

        if (onCallConfigPO.getStartTime().isBefore(onCallConfigPO.getEndTime()) ||
                onCallConfigPO.getStartTime().equals(onCallConfigPO.getEndTime())) {
            return ((now.equals(onCallConfigPO.getStartTime()) || now.isAfter(onCallConfigPO.getStartTime())) &&
                    (now.equals(onCallConfigPO.getEndTime()) || now.isBefore(onCallConfigPO.getEndTime())));
        }

        LocalTime midNight = LocalTime.of(23, 59, 59);
        LocalTime firstTimeOfDay = LocalTime.of(0, 0, 0);

        return ((now.equals(onCallConfigPO.getStartTime()) || now.isAfter(onCallConfigPO.getStartTime())) &&
                (now.equals(midNight) || now.isBefore(midNight)))
                ||
                ((now.equals(firstTimeOfDay) || now.isAfter(firstTimeOfDay)) &&
                        (now.equals(onCallConfigPO.getEndTime()) || now.isBefore(onCallConfigPO.getEndTime())));
    }
}
