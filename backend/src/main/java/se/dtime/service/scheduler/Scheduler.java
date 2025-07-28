package se.dtime.service.scheduler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.repository.SystemPropertyRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.system.EmailSender;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class Scheduler {
    static final String EMAIL_REMINDER_PROPERTY = "Email reminder";

    @Autowired
    private SystemPropertyRepository systemPropertyRepository;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailSender emailSender;

    @Scheduled(cron = "0 0 10 27-31 * ?")
    public void emailReminder() {
        log.info("Start send email reminder");
        SystemPropertyPO systemPropertyPO = systemPropertyRepository.findByName(EMAIL_REMINDER_PROPERTY);
        if (systemPropertyPO == null) {
            log.warn("No email reminder will be sent out. Property {} not found", EMAIL_REMINDER_PROPERTY);
            return;
        } else if (!"true".equals(systemPropertyPO.getValue())) {
            log.info("No email reminder will be sent out");
            return;
        }

        LocalDate now = calendarService.getNowDate();
        LocalDate lastWorkingDayOfMonth = calendarService.getLastWorkingDayOfMonth(now);

        if (now.equals(lastWorkingDayOfMonth)) {
            log.info("Send email reminder");
            List<UserPO> userPOList = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
            userPOList.forEach(u -> emailSender.sendReminderEmail(u.getEmail()));
        }
    }
}
