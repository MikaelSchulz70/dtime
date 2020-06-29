package se.dtime.service.basis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.MonthlyCheckPO;
import se.dtime.model.ReportDates;
import se.dtime.model.basis.InvoiceBasis;
import se.dtime.model.basis.InvoiceCompanyBasis;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.repository.MonthlyCheckRepository;
import se.dtime.repository.RateRepository;
import se.dtime.repository.jdbc.BasisRepository;
import se.dtime.service.calendar.CalendarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class BasisService {
    @Autowired
    private BasisRepository basisRepository;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private MonthlyCheckRepository monthlyCheckRepository;
    @Autowired
    private MonthlyCheckConverter monthlyCheckConverter;
    @Autowired
    private MonthlyCheckValidator monthlyCheckValidator;

    public InvoiceBasis getCurrentInvoiceBasis() {
        ReportDates reportDates = getReportDates(calendarService.getNowDate());
        return getInvoiceBasis(reportDates);
    }

    public InvoiceBasis getNextInvoiceBasis(LocalDate date) {
        ReportDates reportDates = getReportDates(getNextDate(date));
        return getInvoiceBasis(reportDates);
    }

    public InvoiceBasis getPreviousInvoiceBasis(LocalDate date) {
        ReportDates reportDates = getReportDates(getPreviousDate(date));
        return getInvoiceBasis(reportDates);
    }

    InvoiceBasis getInvoiceBasis(ReportDates reportDates) {
        InvoiceBasis invoiceBasis = basisRepository.getInvoiceBasis(reportDates.getFromDate(), reportDates.getToDate());

        LocalDate date = LocalDate.of(reportDates.getFromDate().getYear(), reportDates.getFromDate().getMonth(), 1);

        invoiceBasis.getInvoiceCompanyBases().forEach(c -> {
            CompanyPO companyPO = new CompanyPO(c.getIdCompany());
            MonthlyCheckPO monthlyCheckPO = monthlyCheckRepository.findByCompanyAndDate(companyPO, date);
            if (monthlyCheckPO == null) {
                monthlyCheckPO = new MonthlyCheckPO();
                monthlyCheckPO.setCompany(companyPO);
                monthlyCheckPO.setDate(date);
            }
            c.setMonthlyCheck(monthlyCheckConverter.toModel(monthlyCheckPO));
        });

        BigDecimal hours = invoiceBasis.getInvoiceCompanyBases().stream().
                map(InvoiceCompanyBasis::getHours).
                filter(Objects::nonNull).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumCustomer = invoiceBasis.getInvoiceCompanyBases().stream().
                map(InvoiceCompanyBasis::getSumCustomer).
                filter(Objects::nonNull).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumSubContractor = invoiceBasis.getInvoiceCompanyBases().stream().
                map(InvoiceCompanyBasis::getSumSubcontractor).
                filter(Objects::nonNull).
                reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumFixRate = invoiceBasis.getInvoiceCompanyBases().stream().
                map(InvoiceCompanyBasis::getSumFixRate).
                filter(Objects::nonNull).
                reduce(BigDecimal.ZERO, BigDecimal::add);

        invoiceBasis.setHours(hours);
        invoiceBasis.setSumCustomer(sumCustomer);
        invoiceBasis.setSumSubcontractor(sumSubContractor);
        invoiceBasis.setSumFixRate(sumFixRate);

        return invoiceBasis;
    }

    LocalDate getNextDate(LocalDate date) {
        return date.plusMonths(1);
    }

    LocalDate getPreviousDate(LocalDate date) {
        return date.minusMonths(1);
    }

    ReportDates getReportDates(LocalDate date) {
        LocalDate fromDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate toDate = date.withDayOfMonth(date.lengthOfMonth());
        return new ReportDates(fromDate, toDate);
    }

    public MonthlyCheck addUpdateMonthlyCheck(MonthlyCheck monthlyCheck) {
        monthlyCheckValidator.validateMonthlyCheck(monthlyCheck);
        MonthlyCheckPO monthlyCheckPO = monthlyCheckRepository.findByCompanyAndDate(new CompanyPO(monthlyCheck.getCompanyId()), monthlyCheck.getDate());
        if (monthlyCheckPO != null) {
            monthlyCheckPO.setInvoiceVerified(monthlyCheck.isInvoiceVerified());
            monthlyCheckPO.setInvoiceSent(monthlyCheck.isInvoiceSent());
        } else {
            monthlyCheckPO = monthlyCheckConverter.toPO(monthlyCheck);
        }
        return monthlyCheckConverter.toModel(monthlyCheckRepository.save(monthlyCheckPO));
    }
}
