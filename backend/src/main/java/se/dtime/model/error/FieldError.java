package se.dtime.model.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldError {
    private String fieldName;
    private String fieldError;

    public FieldError(String fieldName, String fieldError) {
        this.fieldName = fieldName;
        this.fieldError = fieldError;
    }
}
