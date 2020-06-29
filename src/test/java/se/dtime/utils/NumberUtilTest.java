package se.dtime.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberUtilTest {

    @Test
    public void roundAndScaleTest() {
        assertEquals(0, NumberUtil.divideRoundAndScalePcp(5, 0), 0.01);
        assertEquals(41.67, NumberUtil.divideRoundAndScalePcp(5, 12), 0.01);
    }

    @Test
    public void scaleTest() {
        assertEquals(42.12, NumberUtil.scale(42.123123), 0.01);
    }
}