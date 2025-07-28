package se.dtime.model.report;

import lombok.*;
import se.dtime.utils.NumberUtil;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUserUserReport {
    private long userId;
    private String fullName;
    private double totalHours = 0;
    private double totalDays;

    public double getTotalDaysScaled() {
        return NumberUtil.scale(totalHours / 8);
    }
}
