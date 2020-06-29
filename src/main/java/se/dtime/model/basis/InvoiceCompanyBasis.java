package se.dtime.model.basis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class InvoiceCompanyBasis {
    private long idCompany;
    private String companyName;
    private BigDecimal hours;
    private BigDecimal sumCustomer;
    private BigDecimal sumSubcontractor;
    private BigDecimal hoursOnCall;
    private BigDecimal sumOnCall;
    private BigDecimal sumSubcontractorOnCall;
    private BigDecimal sumFixRate;
    private List<InvoiceAssignmentBasis> invoiceAssignmentBasis;
    private List<InvoiceAssignmentBasis> invoiceAssignmentBasisOnCall;
    private List<InvoiceFixRateBasis> invoiceFixRateBases;

    private MonthlyCheck monthlyCheck;
}
