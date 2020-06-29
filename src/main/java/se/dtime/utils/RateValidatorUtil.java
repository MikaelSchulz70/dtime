package se.dtime.utils;

import java.time.LocalDate;

public class RateValidatorUtil {

    public static boolean isDateRangeOverlapping(LocalDate fromDate1, LocalDate toDate1, LocalDate fromDate2, LocalDate toDate2) {
        return (toDate2 == null || fromDate1 == null || !fromDate1.isAfter(toDate2))
                && (toDate1 == null || fromDate2 == null || !toDate1.isBefore(fromDate2));
    }
}