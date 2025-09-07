package se.dtime.restcontroller;

import org.junit.jupiter.api.Test;
import se.dtime.model.error.FieldError;
import se.dtime.restcontroller.error.DataIntegrityViolationHandler;

import static org.junit.jupiter.api.Assertions.*;

public class DataIntegrityViolationHandlerTest {

    private final DataIntegrityViolationHandler dataIntegrityViolationHandler = new DataIntegrityViolationHandler();

    @Test
    public void getFieldErrorTest() {
        FieldError fieldError = dataIntegrityViolationHandler.getFieldError(" asdfasd email adfasdfasdfa");
        assertNull(fieldError);

        fieldError = dataIntegrityViolationHandler.getFieldError("uc_email");
        assertNotNull(fieldError);
        assertEquals("email", fieldError.getFieldName());
    }
}