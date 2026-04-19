package se.dtime.model.report;

import lombok.*;
import se.dtime.model.TaskType;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillableTaskTypeReport {
    private TaskType taskType;
    private Boolean isBillable;
    private BigDecimal totalHours;
    private long taskCount;
    private String description;
}