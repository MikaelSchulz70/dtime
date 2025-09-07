package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
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
    private final SystemPropertyRepository systemPropertyRepository;
    private final SystemConverter systemConverter;
    private final SystemValidator systemValidator;

    public SystemService(SystemPropertyRepository systemPropertyRepository, SystemConverter systemConverter, SystemValidator systemValidator, EmailSender emailSender, UserRepository userRepository) {
        this.systemPropertyRepository = systemPropertyRepository;
        this.systemConverter = systemConverter;
        this.systemValidator = systemValidator;
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

    public SystemConfiguration getSystemConfig() {
        return SystemConfiguration.builder()
                .systemProperties(getSystemProperties())
                .build();
    }
}
