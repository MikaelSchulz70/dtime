package se.dtime.model.report;

import lombok.*;
import se.dtime.utils.NumberUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUserReport {
    private Long accountId;
    private String accountName;
    private Long taskId;
    private String taskName;
    private BigDecimal totalHours;
    private BigDecimal totalDays;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<TaskUserUserReport> taskUserUserReports;

    public BigDecimal getTotalDaysScaled() {
        return NumberUtil.scaleBigDecimal(totalHours.divide(BigDecimal.valueOf(8)));
    }
}
