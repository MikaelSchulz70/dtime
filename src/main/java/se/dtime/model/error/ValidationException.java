package se.dtime.model.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends BaseException {
    private String fieldName;

    public ValidationException(String messageKey) {
        super(messageKey);
    }

    public ValidationException(String fieldName, String messageKey) {
        super(messageKey);
        this.fieldName = fieldName;
    }

    public boolean isFieldName() {
        return fieldName != null;
    }
}
