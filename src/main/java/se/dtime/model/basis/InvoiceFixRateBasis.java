package se.dtime.model.basis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class InvoiceFixRateBasis {
    private long idProject;
    private String projectName;
    private boolean isOnCall;
    private BigDecimal hours;
    private BigDecimal fixRate;
    private BigDecimal fixSubContractorRate;
    private String comment;
}
