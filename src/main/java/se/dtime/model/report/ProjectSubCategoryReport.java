package se.dtime.model.report;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSubCategoryReport {
    private boolean provision;
    private boolean internal;
    private boolean onCall;
    private double totalHours;
    private double totalHoursPcp;
}
