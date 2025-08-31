package se.dtime.service.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.SystemPropertyDB;
import se.dtime.model.SystemPropertyType;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.SystemPropertyRepository;

@Service
public class SystemValidator extends ValidatorBase<SystemPropertyDB>  {
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;

    @Override
    public void validateAdd(SystemPropertyDB entity) {

    }

    @Override
    public void validateDelete(long idEntity) {

    }

    @Override
    public void validateUpdate(SystemPropertyDB systemProperty) {
        SystemPropertyPO systemPropertyPO = systemPropertyRepository.findById(systemProperty.getId()).orElseThrow(() -> new NotFoundException("system.property.not.found"));
        validateValueAndType(systemPropertyPO.getSystemPropertyType(), systemProperty.getValue());
    }

    private void validateValueAndType(SystemPropertyType systemPropertyType, String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }

        switch (systemPropertyType) {
            case BOOL:
                check("true".equals(value) || "false".equals(value), "system.property.not.boolean");
                break;
            case FLOAT:
                try {
                    Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    check(false, "system.property.not.float");
                }
                break;
            case INT:
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    check(false, "system.property.not.integer");
                }
                break;
        }
    }


}
