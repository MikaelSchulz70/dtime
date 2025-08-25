package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.SystemConfiguration;
import se.dtime.model.SystemPropertyDB;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.SystemPropertyRepository;
import se.dtime.repository.UserRepository;

@Slf4j
@Service
public class SystemService {
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

    public SystemPropertyDB[] getSystemProperties() {
        return systemConverter.toSystemPropertyModel(systemPropertyRepository.findAll());
    }

    public void updateSystemProperty(SystemPropertyDB systemProperty) {
        systemValidator.validateUpdate(systemProperty);
        SystemPropertyPO systemPropertyPO = systemPropertyRepository.findById(systemProperty.getId()).orElseThrow(() -> new NotFoundException("system.property.not.found"));
        systemPropertyPO.setValue(systemProperty.getValue());
        systemPropertyRepository.save(systemPropertyPO);
    }

    public SystemConfiguration getSystemConfig() {
        return SystemConfiguration.builder()
                .systemProperties(getSystemProperties())
                .build();
    }
}
