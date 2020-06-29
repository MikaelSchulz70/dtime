package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowUpCompanyReport extends FollowUpBaseReport {
    private long idCompany;
    private String companyName;
}
