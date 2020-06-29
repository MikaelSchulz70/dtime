package se.dtime.service.basis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.model.ReportDates;
import se.dtime.model.basis.InvoiceAssignmentBasis;
import se.dtime.model.basis.InvoiceBasis;
import se.dtime.model.basis.InvoiceCompanyBasis;
import se.dtime.model.basis.InvoiceFixRateBasis;
import se.dtime.repository.MonthlyCheckRepository;
import se.dtime.repository.jdbc.BasisRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BasisServiceTest {

    @InjectMocks
    private BasisService basisService;
    @Mock
    private BasisRepository basisRepository;
    @Mock
    private MonthlyCheckRepository monthlyCheckRepository;
    @Mock
    private MonthlyCheckConverter monthlyCheckConverter;

    @Test
    public void getInvoiceBasis() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(1);
        ReportDates reportDates = new ReportDates(fromDate, toDate);

        InvoiceBasis dbInvoiceBasis = InvoiceBasis
                .builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .invoiceCompanyBases(createCompanyBases())
                .build();
        when(basisRepository.getInvoiceBasis(fromDate, toDate)).thenReturn(dbInvoiceBasis);

        InvoiceBasis invoiceBasis = basisService.getInvoiceBasis(reportDates);
        assertEquals(fromDate, invoiceBasis.getFromDate());
        assertEquals(toDate, invoiceBasis.getToDate());
        assertEquals(BigDecimal.valueOf(320), invoiceBasis.getHours());
        assertEquals(BigDecimal.valueOf(20), invoiceBasis.getSumCustomer());
        assertEquals(BigDecimal.valueOf(1), invoiceBasis.getSumSubcontractor());
        assertEquals(BigDecimal.valueOf(2), invoiceBasis.getSumFixRate());
    }

    private List<InvoiceCompanyBasis> createCompanyBases() {
        InvoiceCompanyBasis invoiceCompanyBasis1 = InvoiceCompanyBasis
                .builder()
                .hours(BigDecimal.valueOf(160))
                .sumFixRate(BigDecimal.TEN)
                .hoursOnCall(BigDecimal.TEN)
                .sumCustomer(BigDecimal.TEN)
                .sumFixRate(BigDecimal.ONE)
                .sumOnCall(BigDecimal.TEN)
                .sumSubcontractor(BigDecimal.ONE)
                .invoiceAssignmentBasis(createInvoiceAssignmentBases())
                .invoiceAssignmentBasisOnCall(createInvoiceAssignmnetOnCallBases())
                .invoiceFixRateBases(createInvoiceFixRateBases())
                .build();

        InvoiceCompanyBasis invoiceCompanyBasis2 = InvoiceCompanyBasis
                .builder()
                .hours(BigDecimal.valueOf(160))
                .sumFixRate(BigDecimal.TEN)
                .hoursOnCall(BigDecimal.TEN)
                .sumCustomer(BigDecimal.TEN)
                .sumFixRate(BigDecimal.ONE)
                .sumOnCall(BigDecimal.TEN)
                .invoiceAssignmentBasis(createInvoiceAssignmentBases())
                .invoiceAssignmentBasisOnCall(createInvoiceAssignmnetOnCallBases())
                .invoiceFixRateBases(createInvoiceFixRateBases())
                .build();

        return Arrays.asList(invoiceCompanyBasis1, invoiceCompanyBasis2);
    }

    private List<InvoiceFixRateBasis> createInvoiceFixRateBases() {
        InvoiceFixRateBasis invoiceFixRateBasis1 = InvoiceFixRateBasis
                .builder()
                .fixRate(BigDecimal.valueOf(50000))
                .fixSubContractorRate(BigDecimal.valueOf(45000))
                .hours(BigDecimal.TEN)
                .build();

        InvoiceFixRateBasis invoiceFixRateBasis2 = InvoiceFixRateBasis
                .builder()
                .fixRate(BigDecimal.valueOf(30000))
                .fixSubContractorRate(BigDecimal.valueOf(25000))
                .hours(BigDecimal.TEN)
                .build();
        return Arrays.asList(invoiceFixRateBasis1, invoiceFixRateBasis2);
    }

    private List<InvoiceAssignmentBasis> createInvoiceAssignmnetOnCallBases() {
        InvoiceAssignmentBasis invoiceAssignmentBasis = InvoiceAssignmentBasis
                .builder()
                .isFixRate(false)
                .sumCustomer(BigDecimal.valueOf(20000))
                .sumSubcontractor(BigDecimal.valueOf(19000))
                .customerRate(BigDecimal.valueOf(2000))
                .subContractorRate(BigDecimal.valueOf(1900))
                .hours(BigDecimal.valueOf(10))
                .build();
        return Arrays.asList(invoiceAssignmentBasis);
    }

    private List<InvoiceAssignmentBasis> createInvoiceAssignmentBases() {
        InvoiceAssignmentBasis invoiceAssignmentBasis1 = InvoiceAssignmentBasis
                .builder()
                .isFixRate(false)
                .sumCustomer(BigDecimal.valueOf(20000))
                .sumSubcontractor(BigDecimal.valueOf(19000))
                .customerRate(BigDecimal.valueOf(2000))
                .subContractorRate(BigDecimal.valueOf(1900))
                .hours(BigDecimal.valueOf(10))
                .build();
        InvoiceAssignmentBasis invoiceAssignmentBasis2 = InvoiceAssignmentBasis
                .builder()
                .isFixRate(false)
                .sumCustomer(BigDecimal.valueOf(5000))
                .sumSubcontractor(BigDecimal.valueOf(4500))
                .customerRate(BigDecimal.valueOf(1000))
                .subContractorRate(BigDecimal.valueOf(900))
                .hours(BigDecimal.valueOf(5))
                .build();
        InvoiceAssignmentBasis invoiceAssignmentBasis3 = InvoiceAssignmentBasis
                .builder()
                .isFixRate(false)
                .sumCustomer(BigDecimal.valueOf(9000))
                .sumSubcontractor(BigDecimal.valueOf(8000))
                .customerRate(BigDecimal.valueOf(900))
                .subContractorRate(BigDecimal.valueOf(800))
                .hours(BigDecimal.valueOf(10))
                .build();

        return Arrays.asList(invoiceAssignmentBasis1, invoiceAssignmentBasis2, invoiceAssignmentBasis3);
    }
}