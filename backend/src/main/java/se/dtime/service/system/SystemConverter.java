package se.dtime.service.system;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.SpecialDay;
import se.dtime.model.SystemPropertyDB;

import java.util.List;

@Service
public class SystemConverter {

    public SystemPropertyDB[] toSystemPropertyModel(List<SystemPropertyPO> systemPropertyPOS) {
        if (systemPropertyPOS == null) {
            return null;
        }

        List<SystemPropertyDB> systemProperties = systemPropertyPOS.stream().map(this::toModel).toList();

        return systemProperties.toArray(new SystemPropertyDB[0]);
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

    public SpecialDay[] toSpecialDays(List<SpecialDayPO> specialDayPOS) {
        if (specialDayPOS == null) {
            return null;
        }

        List<SpecialDay> systemProperties = specialDayPOS.stream().map(this::toModel).toList();

        return systemProperties.toArray(new SpecialDay[0]);
    }

    private SpecialDay toModel(SpecialDayPO specialDayPO) {
        if (specialDayPO == null) {
            return null;
        }

        return SpecialDay.builder().id(specialDayPO.getId())
                .name(specialDayPO.getName())
                .dayType(specialDayPO.getDayType())
                .date(specialDayPO.getDate())
                .build();
    }
}
