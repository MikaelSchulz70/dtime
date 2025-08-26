package se.dtime.service.scheduler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.dtime.config.EmailSendConfig;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.system.EmailSender;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class Scheduler {
    
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private EmailSendConfig emailSendConfig;

    @Scheduled(cron = "0 0 10 27-31 * ?")
    public void emailReminder() {
        log.info("Starting scheduled email reminder check");
        
        if (!emailSendConfig.isMailEnabled()) {
            log.info("Email reminders are disabled - mail.enabled is false");
            return;
        }

        LocalDate now = calendarService.getNowDate();
        LocalDate lastWorkingDayOfMonth = calendarService.getLastWorkingDayOfMonth(now);

        if (now.equals(lastWorkingDayOfMonth)) {
            log.info("Sending scheduled email reminders - today is the last working day of the month");
            List<UserPO> userPOList = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
            userPOList.forEach(u -> emailSender.sendReminderEmail(u.getEmail()));
        } else {
            log.info("Not sending email reminders - today is not the last working day of the month");
        }
    }
}
