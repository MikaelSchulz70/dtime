package se.dtime.restcontroller;

import org.junit.Test;
import se.dtime.model.error.FieldError;
import se.dtime.restcontroller.error.DataIntegrityViolationHandler;

import static org.junit.Assert.*;

public class DataIntegrityViolationHandlerTest {

    private DataIntegrityViolationHandler dataIntegrityViolationHandler = new DataIntegrityViolationHandler();

    @Test
    public void getFieldErrorTest() {
        FieldError fieldError = dataIntegrityViolationHandler.getFieldError(" asdfasd username adfasdfasdfa");
        assertNull(fieldError);

        fieldError = dataIntegrityViolationHandler.getFieldError("uc_username");
        assertNotNull(fieldError);
        assertEquals("userName", fieldError.getFieldName());
    }
}