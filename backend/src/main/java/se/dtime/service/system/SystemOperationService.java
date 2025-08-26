package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.report.UnclosedUserReport;
import se.dtime.repository.UserRepository;
import se.dtime.service.report.TimeReportStatusService;

import java.util.List;

@Slf4j
@Service
public class SystemOperationService {
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TimeReportStatusService timeReportStatusService;

    public void sendMailReminder() {
        List<UserPO> userPOList = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
        userPOList.forEach(u -> emailSender.sendReminderEmail(u.getEmail()));
    }

    public void sendMailReminderToUnclosedUsers() {
        log.info("Sending email reminders to users with unclosed time reports");
        UnclosedUserReport report = timeReportStatusService.getCurrentUnclosedUsers();
        
        if (report.getUnclosedUsers() == null || report.getUnclosedUsers().isEmpty()) {
            log.info("No unclosed users found, no emails will be sent");
            return;
        }

        int emailCount = 0;
        for (var unclosedUser : report.getUnclosedUsers()) {
            if (unclosedUser.getEmail() != null && !unclosedUser.getEmail().isEmpty()) {
                emailSender.sendReminderEmail(unclosedUser.getEmail());
                emailCount++;
            }
        }
        
        log.info("Email reminders sent to {} users with unclosed time reports", emailCount);
    }
}
