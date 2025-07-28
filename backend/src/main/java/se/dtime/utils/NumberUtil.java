package se.dtime.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

    private NumberUtil() {

    }

    public static double divideRoundAndScalePcp(double nominator, double denominator) {
        if (denominator == 0) {
            return 0;
        }
        return new BigDecimal((100 * nominator) / denominator).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double scale(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}
