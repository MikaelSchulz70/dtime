package se.dtime.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static String formateDate(LocalDate date) {
        return date.format(dateFormatter);
    }

    public static String formateDateTime(LocalDateTime dateTime) {
        return dateTime.format(dateTimeFormatter);
    }
}
