package se.dtime.model.report;

import lombok.*;
import se.dtime.utils.NumberUtil;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUserUserReport {
    private long userId;
    private String fullName;
    private BigDecimal totalHours = BigDecimal.ZERO;
    private BigDecimal totalDays;

    public BigDecimal getTotalDaysScaled() {
        return NumberUtil.scaleBigDecimal(totalHours.divide(BigDecimal.valueOf(8)));
    }
}
