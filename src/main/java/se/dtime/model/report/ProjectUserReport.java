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
public class ProjectUserReport {
    private Long idCompany;
    private String companyName;
    private Long idProject;
    private String projectName;
    private double totalHours;
    private double totalDays;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<ProjectUserUserReport> projectUserUserReports;

    public double getTotalDaysScaled() {
        return NumberUtil.scale(totalHours / 8);
    }
}
