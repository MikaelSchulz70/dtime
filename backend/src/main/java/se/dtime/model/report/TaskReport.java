package se.dtime.model.report;

import lombok.*;
import se.dtime.model.TaskType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskReport {
    private Long accountId;
    private String accountName;
    private Long taskId;
    private String taskName;
    private TaskType taskType;
    private Boolean isBillable;
    private double totalHours;
}
