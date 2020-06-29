package se.dtime.service.system;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.SystemPropertyDB;
import se.dtime.model.SystemPropertyType;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.SystemPropertyRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SystemValidatorTest {
    @InjectMocks
    private SystemValidator systemValidator;
    @Mock
    private SystemPropertyRepository systemPropertyRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateUpdateNotFound() {
        SystemPropertyDB systemProperty = new SystemPropertyDB();
        systemProperty.setId(1L);

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("system.property.not.found");
        systemValidator.validateUpdate(systemProperty);
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
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("system.property.not.boolean");
        systemValidator.validateUpdate(systemProperty);
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
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("system.property.not.float");
        systemValidator.validateUpdate(systemProperty);
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
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("system.property.not.integer");
        systemValidator.validateUpdate(systemProperty);
    }
}