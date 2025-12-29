package se.dtime.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

    public static String truncate(String str, int length) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return str.substring(0, Math.min(str.length(), length));
    }
}
