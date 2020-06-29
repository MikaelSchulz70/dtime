package se.dtime.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void truncate() {
        assertNull(StringUtil.truncate(null, 100));
        assertEquals("", StringUtil.truncate("", 100));
        assertEquals("hej", StringUtil.truncate("hej hopp", 3));
        assertEquals("hej hopp", StringUtil.truncate("hej hopp", 10));
    }
}