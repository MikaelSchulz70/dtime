package se.dtime.service.oncall;

import org.junit.Test;
import se.dtime.dbmodel.oncall.OnCallConfigPO;

import java.time.LocalTime;

import static org.junit.Assert.*;

public class OnCallServiceTest {
    private OnCallService onCallService = new OnCallService();


    @Test
    public void isWithinOnCallTimeNoConfig() {
        LocalTime time = LocalTime.of(12, 0, 0);

        assertFalse(onCallService.isWithinOnCallTime(null, time));

        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        assertFalse(onCallService.isWithinOnCallTime(onCallConfigPO, time));
    }

    @Test
    public void isWithinOnCallTimeNoEndTime() {
        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setStartTime(LocalTime.of(10, 0, 0));

        LocalTime time = LocalTime.of(9, 0, 0);
        assertFalse(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(10, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(11, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));
    }

    @Test
    public void isWithinOnCallTimeNoStartTime() {
        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setEndTime(LocalTime.of(10, 0, 0));

        LocalTime time = LocalTime.of(11, 0, 0);
        assertFalse(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(10, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(7, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));
    }

    @Test
    public void isWithinOnCallTimeNoStartAndEndTime() {
        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setStartTime(LocalTime.of(10, 0, 0));
        onCallConfigPO.setEndTime(LocalTime.of(14, 0, 0));

        LocalTime time = LocalTime.of(9, 59, 59);
        assertFalse(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(14, 0, 1);
        assertFalse(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(12, 15, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));
    }

    @Test
    public void isWithinOnCallTimeNoEndTimeBeforeStartTime() {
        OnCallConfigPO onCallConfigPO = new OnCallConfigPO();
        onCallConfigPO.setStartTime(LocalTime.of(17, 0, 0));
        onCallConfigPO.setEndTime(LocalTime.of(7, 0, 0));

        LocalTime time = LocalTime.of(16, 0, 0);
        assertFalse(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(17, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(22, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(23, 59, 59);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(0, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(6, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));

        time = LocalTime.of(7, 0, 0);
        assertTrue(onCallService.isWithinOnCallTime(onCallConfigPO, time));
    }
}