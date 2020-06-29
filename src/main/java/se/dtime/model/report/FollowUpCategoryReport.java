package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;
import se.dtime.model.ProjectCategory;

import java.util.List;

@Getter
@Setter
public class FollowUpCategoryReport extends FollowUpBaseReport {
    private ProjectCategory projectCategory;
    private List<FollowUpUserReport> followUpUserReports;

    public FollowUpUserReport getFollowUpUserReport(long idUser) {
        return followUpUserReports.stream().filter(r -> r.getIdUser() == idUser).findFirst().orElse(null);
    }
}
