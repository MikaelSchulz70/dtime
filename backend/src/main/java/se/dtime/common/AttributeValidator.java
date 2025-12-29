package se.dtime.common;

import org.apache.commons.lang3.StringUtils;
import se.dtime.model.Attribute;
import se.dtime.model.error.ValidationException;

public abstract class AttributeValidator {

    public abstract void validate(Attribute attribute) throws ValidationException;

    protected void check(boolean isOk, String attributeName, String messageKey) {
        if (!isOk) {
            throw new ValidationException(attributeName, messageKey);
        }
    }

    protected void checkLength(Attribute attribute, final int minLength, final int maxLength, String messageKey) {
        if (StringUtils.isEmpty(attribute.getValue()) || attribute.getValue().length() < minLength ||
                attribute.getValue().length() > maxLength) {
            throw new ValidationException(attribute.getName(), messageKey);
        }
    }
}
