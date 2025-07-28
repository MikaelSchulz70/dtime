package se.dtime.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.dbmodel.BasePO;
import se.dtime.model.UserExt;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDateTime;

public abstract class BaseConverter {
    @Autowired
    private CalendarService calendarService;

    public void updateBaseData(BasePO basePO) {
        LocalDateTime now = calendarService.getNowDateTime();
        basePO.setCreateDateTime(now);
        basePO.setUpdatedDateTime(now);

        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        basePO.setUpdatedBy(userExt.getId());
        basePO.setCreatedBy(userExt.getId());
    }
}
