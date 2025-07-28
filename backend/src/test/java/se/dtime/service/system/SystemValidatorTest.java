package se.dtime.service.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.SystemPropertyDB;
import se.dtime.model.SystemPropertyType;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.SystemPropertyRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SystemValidatorTest {
    @InjectMocks
    private SystemValidator systemValidator;
    @Mock
    private SystemPropertyRepository systemPropertyRepository;

    @Test
    public void validateUpdateNotFound() {
        SystemPropertyDB systemProperty = new SystemPropertyDB();
        systemProperty.setId(1L);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            systemValidator.validateUpdate(systemProperty);
        });
        assert exception.getMessage().contains("system.property.not.found");
    }

    @Test
    public void validateUpdateBoolValue() {
        SystemPropertyDB systemProperty = new SystemPropertyDB();
        systemProperty.setId(1L);

        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setSystemPropertyType(SystemPropertyType.BOOL);
        when(systemPropertyRepository.findById(1L)).thenReturn(Optional.of(systemPropertyPO));

        systemProperty.setValue("true");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("false");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue(null);
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("ABC");
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            systemValidator.validateUpdate(systemProperty);
        });
        assert exception.getMessage().contains("system.property.not.boolean");
    }

    @Test
    public void validateUpdateFloatValue() {
        SystemPropertyDB systemProperty = new SystemPropertyDB();
        systemProperty.setId(1L);

        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setSystemPropertyType(SystemPropertyType.FLOAT);
        when(systemPropertyRepository.findById(1L)).thenReturn(Optional.of(systemPropertyPO));

        systemProperty.setValue("1.12");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("2");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue(null);
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("ABC");
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            systemValidator.validateUpdate(systemProperty);
        });
        assert exception.getMessage().contains("system.property.not.float");
    }

    @Test
    public void validateUpdateIntegerValue() {
        SystemPropertyDB systemProperty = new SystemPropertyDB();
        systemProperty.setId(1L);

        SystemPropertyPO systemPropertyPO = new SystemPropertyPO();
        systemPropertyPO.setSystemPropertyType(SystemPropertyType.INT);
        when(systemPropertyRepository.findById(1L)).thenReturn(Optional.of(systemPropertyPO));

        systemProperty.setValue("2");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue(null);
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("");
        systemValidator.validateUpdate(systemProperty);

        systemProperty.setValue("ABC");
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            systemValidator.validateUpdate(systemProperty);
        });
        assert exception.getMessage().contains("system.property.not.integer");
    }
}