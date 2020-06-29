package se.dtime.model.report;

import lombok.*;
import se.dtime.model.ProjectCategory;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCategoryReport {
    private ProjectCategory projectCategory;
    private double totalHours = 0;
    private double totalHoursPcp = 0;
    List<ProjectSubCategoryReport> projectSubCategoryReports;
}
