package se.dtime.model.basis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class InvoiceAssignmentBasis {
    private long idAssignment;
    private String projectName;
    private String userName;
    private boolean isOnCall;
    private boolean isFixRate;
    private BigDecimal hours;
    private BigDecimal customerRate;
    private BigDecimal sumCustomer;
    private BigDecimal subContractorRate;
    private BigDecimal sumSubcontractor;
    private String rateComment;
    private String comment;
}
