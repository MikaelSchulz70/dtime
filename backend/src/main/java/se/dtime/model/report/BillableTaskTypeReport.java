package se.dtime.model.report;

import lombok.*;
import se.dtime.model.TaskType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillableTaskTypeReport {
    private TaskType taskType;
    private Boolean isBillable;
    private double totalHours;
    private long taskCount;
    private String description;
}