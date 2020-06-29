package se.dtime.model.basis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class InvoiceBasis {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal hours;
    private BigDecimal sumCustomer;
    private BigDecimal sumSubcontractor;
    private BigDecimal sumFixRate;

    private List<InvoiceCompanyBasis> invoiceCompanyBases;
}
