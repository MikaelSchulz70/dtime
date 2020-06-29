package se.dtime.model.report;

import lombok.*;
import se.dtime.model.ProjectCategory;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReport {
    private Long idCompany;
    private String companyName;
    private Long idProject;
    private String projectName;
    private boolean provision;
    private boolean internal;
    private boolean onCall;
    private double totalHours;
    private ProjectCategory projectCategory;
}
