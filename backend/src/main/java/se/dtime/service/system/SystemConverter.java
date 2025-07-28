package se.dtime.service.system;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.PublicHolidayPO;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.PublicHoliday;
import se.dtime.model.SystemPropertyDB;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemConverter {

    public SystemPropertyDB[] toSystemPropertyModel(List<SystemPropertyPO> systemPropertyPOS) {
        if (systemPropertyPOS == null) {
            return null;
        }

        List<SystemPropertyDB> systemProperties = systemPropertyPOS.stream().map(s -> toModel(s)).collect(Collectors.toList());

        return systemProperties.toArray(new SystemPropertyDB[systemProperties.size()]);
    }

    public SystemPropertyPO toPO(SystemPropertyDB systemProperty) {
        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setName(systemProperty.getName());
        systemPropertyPO.setValue(systemProperty.getValue());
        return systemPropertyPO;
    }

    public SystemPropertyDB toModel(SystemPropertyPO systemPropertyPO) {
        if (systemPropertyPO == null) {
            return null;
        }

        return SystemPropertyDB.builder().id(systemPropertyPO.getId()).
                name(systemPropertyPO.getName()).
                value(systemPropertyPO.getValue()).
                systemPropertyType(systemPropertyPO.getSystemPropertyType()).
                description(systemPropertyPO.getDescription()).
                build();
    }

    public PublicHoliday[] toPublicHolidayModel(List<PublicHolidayPO> publicHolidayPOS) {
        if (publicHolidayPOS == null) {
            return null;
        }

        List<PublicHoliday> systemProperties = publicHolidayPOS.stream().map(s -> toModel(s)).collect(Collectors.toList());

        return systemProperties.toArray(new PublicHoliday[systemProperties.size()]);
    }

    private PublicHoliday toModel(PublicHolidayPO publicHolidayPO) {
        if (publicHolidayPO == null) {
            return null;
        }

        return PublicHoliday.builder().id(publicHolidayPO.getId()).
                name(publicHolidayPO.getName()).
                isWorkday(publicHolidayPO.isWorkday()).
                build();
    }
}
