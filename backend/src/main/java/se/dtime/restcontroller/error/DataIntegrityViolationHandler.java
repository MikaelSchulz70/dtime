package se.dtime.restcontroller.error;

import org.springframework.stereotype.Service;
import se.dtime.model.error.FieldError;

import java.util.HashMap;
import java.util.Map;

@Service
public class DataIntegrityViolationHandler {
    private static final String[] KNOWN_CONSTRAINT_ERRORS = {"uc_email"};
    private static final Map<String, FieldError> FIELD_ERRORS = new HashMap<>();

    static {
        FIELD_ERRORS.put("uc_email", new FieldError("email", "Email already in use"));
    }

    public FieldError getFieldError(String errorMsg) {
        for (String constraintError : KNOWN_CONSTRAINT_ERRORS) {
            if (errorMsg.contains(constraintError)) {
                return FIELD_ERRORS.get(constraintError);
            }
        }

        return null;
    }
}
