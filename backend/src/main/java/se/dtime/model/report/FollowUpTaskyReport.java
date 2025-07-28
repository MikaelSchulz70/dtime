package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FollowUpTaskyReport extends FollowUpBaseReport {
    private List<FollowUpUserReport> followUpUserReports;

    public FollowUpUserReport getFollowUpUserReport(long userId) {
        return followUpUserReports.stream().filter(r -> r.getuserId() == userId).findFirst().orElse(null);
    }
}
