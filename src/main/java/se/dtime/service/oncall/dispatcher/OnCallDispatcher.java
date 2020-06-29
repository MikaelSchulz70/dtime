package se.dtime.service.oncall.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.common.CommonData;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.oncall.OnCallAlarmPO;
import se.dtime.dbmodel.oncall.OnCallRulePO;
import se.dtime.dbmodel.oncall.OnCallSessionPO;
import se.dtime.model.EmailContainer;
import se.dtime.model.EmailPollContainer;
import se.dtime.model.error.DTimeException;
import se.dtime.model.oncall.OnCallProject;
import se.dtime.model.oncall.OnCallSeverity;
import se.dtime.model.oncall.OnCallStatus;
import se.dtime.model.oncall.OnCallUser;
import se.dtime.repository.OnCallAlarmRepository;
import se.dtime.repository.OnCallRuleRepository;
import se.dtime.repository.OnCallSessionRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.oncall.OnCallOperationService;
import se.dtime.service.oncall.OnCallService;
import se.dtime.service.system.EmailSender;
import se.dtime.service.system.SmsSender;
import se.dtime.utils.StringUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OnCallDispatcher {
    private final static String SMS_SENDER = "DtimeOC";
    private final static String ALARM_TEXT = "Dtime OnCall. Incoming alarm from ";

    @Autowired
    private OnCallService onCallService;
    @Autowired
    private OnCallOperationService onCallOperationService;
    @Autowired
    private OnCallAlarmRepository onCallAlarmRepository;
    @Autowired
    private OnCallRuleRepository onCallRuleRepository;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private OnCallEmailReader onCallEmailReader;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private SmsSender smsSender;
    @Autowired
    private OnCallSessionRepository onCallSessionRepository;

    @Value("sms.phonenumber")
    private String onCallMobileNumber;

    public void dispatch() {
        LocalDateTime now = calendarService.getNowDateTime();
        OnCallSessionPO onCallSessionPO = onCallOperationService.getOnCallSessionPO();

        List<OnCallProject> onCallProjects = onCallService.getOnCallProjects(now);
        if (onCallProjects.isEmpty()) {
            log.info("No one oncall");
            saveOnCallSession(onCallSessionPO, now, "No one oncall (no email read)", false);
            return;
        }

        EmailPollContainer emailPollContainer;
        try {
            emailPollContainer = onCallEmailReader.readEmails(onCallSessionPO.getLastPollDateTime(), now);
        } catch (DTimeException e) {
            saveOnCallSession(onCallSessionPO, now, "Failed to read oncall emails", true);
            return;
        }

        onCallSessionPO.setMailInInboxInLastPoll(emailPollContainer.getMailInInboxInLastPoll());
        onCallSessionPO.setReadInLastPoll(emailPollContainer.getReadInLastPoll());
        onCallSessionPO.setTotalEmails(onCallSessionPO.getTotalEmails() + onCallSessionPO.getReadInLastPoll());

        if (emailPollContainer.getEmailContainers().isEmpty()) {
            log.info("No incoming oncall emails");
            saveOnCallSession(onCallSessionPO, now, "No incoming oncall emails", false);
            return;
        }

        List<OnCallRulePO> onCallRulePOS = onCallRuleRepository.findAll();

        int numberEmailDispatched = 0;
        for (EmailContainer emailContainer : emailPollContainer.getEmailContainers()) {
            if (FloodingChecker.isFlooding(now, emailContainer)) {
                log.warn("Duplicate email received. No action. {} {}", emailContainer.getFrom(), emailContainer.getSubject());
                continue;
            }

            OnCallRulePO onCallRulePO = getMatchingRule(emailContainer, onCallRulePOS);
            if (onCallRulePO == null) {
                log.info("No matching rule for email sender {}", emailContainer.getFrom());
                continue;
            }

            List<OnCallProject> onCallProjectsForEmail = onCallProjects.stream().
                    filter(ocp -> ocp.getIdProject() == onCallRulePO.getProject().getId()).
                    collect(Collectors.toList());

            if (onCallProjectsForEmail.isEmpty()) {
                log.info("No matching project for rule {}", onCallRulePO.getProject().getId());
                continue;
            }

            for (OnCallProject onCallProject : onCallProjectsForEmail) {
                for (OnCallUser onCallUser : onCallProject.getOnCallUsers()) {
                    OnCallAlarmPO onCallAlarmPO = new OnCallAlarmPO();
                    onCallAlarmPO.setUser(new UserPO(onCallUser.getIdUser()));
                    onCallAlarmPO.setProject(new ProjectPO(onCallProject.getIdProject()));
                    onCallAlarmPO.setStatus(OnCallStatus.NEW);

                    String message = "";
                    boolean emailSent = false;
                    if (StringUtils.isEmpty(onCallUser.getEmail())) {
                        message = "No email found. ";
                        log.warn("No email found for user {}", onCallUser.getUserName());
                    } else {
                        try {
                            emailSender.sendForwardOnCallEmail(onCallUser.getEmail(), emailContainer);
                            emailSent = true;
                        } catch (DTimeException e) {
                            message = "Failed to send email. ";
                        }
                    }

                    boolean smsSent = false;
                    if (StringUtils.isEmpty(onCallUser.getMobileNumber())) {
                        message += "No mobile number found. ";
                        log.warn("No mobile number found for user {}", onCallUser.getUserName());
                    }

                    try {
                        smsSender.sendSms(SMS_SENDER, buildSmsBody(onCallProject), onCallUser.getMobileNumber(), onCallMobileNumber);
                        smsSent = true;
                    } catch (Exception e) {
                        message += "Failed to send sms.";
                    }

                    onCallAlarmPO.setEmailSent(emailSent);
                    onCallAlarmPO.setInstantMsgSent(smsSent);
                    onCallAlarmPO.setSender(StringUtil.truncate(emailContainer.getFrom(), 100));
                    onCallAlarmPO.setSubject(StringUtil.truncate(emailContainer.getSubject(), 100));
                    onCallAlarmPO.setMessage(StringUtil.truncate(message, 250));
                    onCallAlarmPO.setOnCallSeverity(OnCallSeverity.ERROR);
                    saveOnCallAlarm(onCallAlarmPO);

                    numberEmailDispatched += (emailSent || smsSent ? 1 : 0);
                }
            }
        }

        onCallSessionPO.setLastPollDateTime(now);
        onCallSessionPO.setMessage("Ok");
        onCallSessionPO.setTotalDispatched(onCallSessionPO.getTotalDispatched() + numberEmailDispatched);
        onCallSessionPO.setDispatchedInLastPoll(numberEmailDispatched);
        onCallSessionRepository.save(onCallSessionPO);
    }

    private String buildSmsBody(OnCallProject onCallProject) {
        return ALARM_TEXT + onCallProject.getCompanyName() + "/" + onCallProject.getProjectName();
    }

    private void saveOnCallAlarm(OnCallAlarmPO onCallAlarmPO) {
        LocalDateTime now = calendarService.getNowDateTime();
        onCallAlarmPO.setCreatedBy(CommonData.SYSTEM_USER_ID);
        onCallAlarmPO.setUpdatedBy(CommonData.SYSTEM_USER_ID);
        onCallAlarmPO.setCreateDateTime(now);
        onCallAlarmPO.setUpdatedDateTime(now);
        onCallAlarmRepository.save(onCallAlarmPO);
    }

    private void saveOnCallSession(OnCallSessionPO onCallSessionPO, LocalDateTime now, String message, boolean isError) {
        onCallSessionPO.setLastPollDateTime(now);
        onCallSessionPO.setMessage(message);
        onCallSessionRepository.save(onCallSessionPO);
    }

    private OnCallRulePO getMatchingRule(EmailContainer emailContainer, List<OnCallRulePO> onCallRulePOS) {
        return onCallRulePOS.stream().filter(ocr -> isMatch(emailContainer, ocr)).findFirst().orElse(null);
    }

    boolean isMatch(EmailContainer emailContainer, OnCallRulePO onCallRulePO) {
        if (!emailContainer.getFrom().contains(onCallRulePO.getFromEmail())) {
            return false;
        }

        if (StringUtils.isEmpty(onCallRulePO.getSubjectCSV()) && StringUtils.isEmpty(onCallRulePO.getBodyCSV())) {
            return false;
        } else if (!StringUtils.isEmpty(onCallRulePO.getSubjectCSV()) && StringUtils.isEmpty(onCallRulePO.getBodyCSV())) {
            return isMatch(onCallRulePO.getSubjectCSVAsArray(), emailContainer.getSubject());
        } else if (StringUtils.isEmpty(onCallRulePO.getSubjectCSV()) && !StringUtils.isEmpty(onCallRulePO.getBodyCSV())) {
            return isMatch(onCallRulePO.getBodyCSVAsArray(), emailContainer.getBody());
        }

        return isMatch(onCallRulePO.getSubjectCSVAsArray(), emailContainer.getSubject()) &&
                isMatch(onCallRulePO.getBodyCSVAsArray(), emailContainer.getBody());
    }

    private boolean isMatch(List<String> valuesToLookFor, String value) {
        return valuesToLookFor.stream().anyMatch(w -> value != null && value.contains(w));
    }


}
