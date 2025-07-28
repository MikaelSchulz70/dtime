package se.dtime.model.report;

import lombok.*;

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
    private double totalHours;
}
