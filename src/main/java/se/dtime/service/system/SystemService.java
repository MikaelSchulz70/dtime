package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.PublicHolidayPO;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.PublicHoliday;
import se.dtime.model.SystemConfiguration;
import se.dtime.model.SystemPropertyDB;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.PublicHolidayRepository;
import se.dtime.repository.SystemPropertyRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.oncall.dispatcher.OnCallDispatcher;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class SystemService {
    @Autowired
    private PublicHolidayRepository publicHolidayRepository;
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;
    @Autowired
    private SystemConverter systemConverter;
    @Autowired
    private SystemValidator systemValidator;
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OnCallDispatcher onCallDispatcher;

    public PublicHoliday[] getPublicHolidays() {
        List<PublicHolidayPO> publicHolidayPOS = publicHolidayRepository.findAll();
        Collections.sort(publicHolidayPOS, Comparator.comparingLong(PublicHolidayPO::getId));
        return systemConverter.toPublicHolidayModel(publicHolidayPOS);
    }

    public SystemPropertyDB[] getSystemProperties() {
        return systemConverter.toSystemPropertyModel(systemPropertyRepository.findAll());
    }

    public void updateSystemProperty(SystemPropertyDB systemProperty) {
        systemValidator.validateUpdate(systemProperty);
        SystemPropertyPO systemPropertyPO = systemPropertyRepository.findById(systemProperty.getId()).orElseThrow(() -> new NotFoundException("system.property.not.found"));
        systemPropertyPO.setValue(systemProperty.getValue());
        systemPropertyRepository.save(systemPropertyPO);
    }

    public SystemPropertyDB getSystemProperty(long id) {
        return systemConverter.toModel(systemPropertyRepository.findById(id).orElseThrow(() -> new NotFoundException("system.property.not.found")));
    }

    public SystemConfiguration getSystemConfig() {
        return SystemConfiguration.builder().
                systemProperties(getSystemProperties()).
                publicHolidays(getPublicHolidays()).
                build();
    }
}
