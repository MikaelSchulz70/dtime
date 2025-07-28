package se.dtime.model.report;

import lombok.*;
import se.dtime.utils.NumberUtil;

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
    private double totalHours;
    private double totalDays;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<TaskUserUserReport> taskUserUserReports;

    public double getTotalDaysScaled() {
        return NumberUtil.scale(totalHours / 8);
    }
}
