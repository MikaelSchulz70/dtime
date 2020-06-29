package se.dtime.utils;

import org.junit.Test;

import java.time.LocalDate;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class RateValidatorUtilTest {

    @Test
    public void isDateRangeOverlapping() {
        assertFalse(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 11, 1), LocalDate.of(2019, 12, 31)));

        assertFalse(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 11, 1),
                LocalDate.of(2019, 12, 30),
                LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 30)));


        assertFalse(RateValidatorUtil.isDateRangeOverlapping(null,
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 11, 1), LocalDate.of(2019, 12, 31)));

        assertFalse(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 11, 1), null));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 11, 2),
                LocalDate.of(2019, 11, 1), LocalDate.of(2019, 12, 31)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 11, 2),
                LocalDate.of(2019, 11, 15),
                LocalDate.of(2019, 11, 1), LocalDate.of(2019, 12, 31)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 10, 15), LocalDate.of(2019, 12, 31)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 10, 15), LocalDate.of(2019, 10, 20)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 10, 30), LocalDate.of(2019, 11, 20)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                LocalDate.of(2019, 9, 30), LocalDate.of(2019, 10, 1)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 9, 1),
                null,
                LocalDate.of(2019, 10, 30), LocalDate.of(2019, 10, 1)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 10, 1),
                LocalDate.of(2019, 10, 30),
                null, LocalDate.of(2019, 10, 1)));

        assertTrue(RateValidatorUtil.isDateRangeOverlapping(LocalDate.of(2019, 11, 1),
                LocalDate.of(2019, 11, 30),
                LocalDate.of(2019, 10, 1), null));

    }
}