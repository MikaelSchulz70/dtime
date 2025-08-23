package se.dtime.service.specialday;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.SpecialDay;
import se.dtime.service.BaseConverter;

import java.time.LocalDateTime;

@Service
public class SpecialDayConverter extends BaseConverter {

    public SpecialDay toModel(SpecialDayPO specialDayPO) {
        if (specialDayPO == null) {
            return null;
        }

        return SpecialDay.builder()
                .id(specialDayPO.getId())
                .name(specialDayPO.getName())
                .dayType(specialDayPO.getDayType())
                .date(specialDayPO.getDate())
                .build();
    }

    public SpecialDayPO toPO(SpecialDay specialDay) {
        if (specialDay == null) {
            return null;
        }

        SpecialDayPO specialDayPO = new SpecialDayPO();
        specialDayPO.setId(specialDay.getId());
        specialDayPO.setName(specialDay.getName());
        specialDayPO.setDayType(specialDay.getDayType());
        specialDayPO.setDate(specialDay.getDate());
        
        // Set audit fields for new entities
        if (specialDay.getId() == null) {
            updateBaseData(specialDayPO);
        } else {
            specialDayPO.setUpdatedDateTime(LocalDateTime.now());
            // For updates, we need to get current user ID
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            long userId = 1L; // Default fallback
            if (principal instanceof se.dtime.model.UserExt userExt) {
                userId = userExt.getId();
            }
            specialDayPO.setUpdatedBy(userId);
        }

        return specialDayPO;
    }
}