package se.dtime.service.oncall.dispatcher;

import org.junit.Test;
import se.dtime.model.EmailContainer;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class FloodingCheckerTest {

    @Test
    public void isFlooding() {
        EmailContainer emailContainer = EmailContainer.builder().from("t@t.se").subject("Error").body("body").build();
        LocalDateTime now = LocalDateTime.now();
        assertFalse(FloodingChecker.isFlooding(now, emailContainer));
        assertTrue(FloodingChecker.isFlooding(now, emailContainer));
        assertTrue(FloodingChecker.isFlooding(now.plusMinutes(59), emailContainer));
        assertFalse(FloodingChecker.isFlooding(now.plusHours(1), emailContainer));
        assertTrue(FloodingChecker.isFlooding(now.plusHours(1).plusMinutes(1), emailContainer));
        assertTrue(FloodingChecker.isFlooding(now.plusHours(1).plusMinutes(30), emailContainer));
        assertTrue(FloodingChecker.isFlooding(now.plusHours(1).plusMinutes(59), emailContainer));
        assertFalse(FloodingChecker.isFlooding(now.plusHours(2), emailContainer));
        assertTrue(FloodingChecker.isFlooding(now.plusHours(2).plusMinutes(1), emailContainer));
    }
}