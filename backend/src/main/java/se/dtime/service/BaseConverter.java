package se.dtime.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.dbmodel.BasePO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.UserExt;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDateTime;

public abstract class BaseConverter {
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private UserRepository userRepository;

    public void updateBaseData(BasePO basePO) {
        LocalDateTime now = calendarService.getNowDateTime();
        basePO.setCreateDateTime(now);
        basePO.setUpdatedDateTime(now);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId;
        
        if (principal instanceof UserExt userExt) {
            userId = userExt.getId();
        } else {
            // For test scenarios with mock users, try to find by authentication name
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            UserPO userPO = userRepository.findByEmail(username);
            if (userPO != null) {
                userId = userPO.getId();
            } else {
                userId = 1L; // Fallback to system user
            }
        }
        
        basePO.setUpdatedBy(userId);
        basePO.setCreatedBy(userId);
    }
}
