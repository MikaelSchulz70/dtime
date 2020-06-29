package se.dtime.model.report;

import lombok.Builder;
import lombok.Getter;
import se.dtime.model.ProjectCategory;

import java.math.BigDecimal;

@Builder
@Getter
public class FollowUpData {
    private long idCompany;
    private String companyName;
    private long idProject;
    private String projectName;
    private boolean isOncall;
    private ProjectCategory category;
    private long idUser;
    private String userName;
    private BigDecimal totalTime;
    private BigDecimal rateCustomer;
    private BigDecimal rateSubcontractor;
    private BigDecimal amount;
    private BigDecimal amountSubcontractor;
    private String comment;
}
