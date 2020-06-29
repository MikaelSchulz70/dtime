package se.dtime.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import se.dtime.model.basis.InvoiceAssignmentBasis;
import se.dtime.model.basis.InvoiceBasis;
import se.dtime.model.basis.InvoiceCompanyBasis;
import se.dtime.model.basis.InvoiceFixRateBasis;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BasisRepository extends JdbcDaoSupport {
    @Autowired
    private DataSource dataSource;

    private final String INVOICE_BASIS =
            "select c.id idcompany, c.name as companyname, p.name as projectname, p.id idproject, p.oncall, " +
                    "a.id idassignment, u.id as iduser, u.firstName, u.lastName, sum(reportedtime) totalTime, " +
                    "r.ratecustomer, r.ratesubcontractor, r.comment " +
                    "from timereport tr " +
                    "join assignment a on a.id = tr.id_assignment " +
                    "join project p on p.id = a.id_project " +
                    "join company c on c.id = p.id_company " +
                    "join users u on a.id_user = u.id " +
                    "left join rate r on r.id_assignment = a.id and (tr.date >= r.fromdate and (tr.date <= r.todate or r.todate is null)) " +
                    "where date >= ? and date <= ? " +
                    "and p.internal = 0 " +
                    "group by c.id, c.name, p.id, p.name, p.oncall, p.fixrate, a.id, u.id, u.firstName, u.lastName, r.ratecustomer, r.ratesubcontractor, r.comment " +
                    "having sum(reportedtime) > 0 " +
                    "order by c.name, p.name, p.oncall desc;";

    private final String FIX_RATE_BASIS = "select p.id_company idCompany, p.id idProject, p.name projectName, p.oncall, " +
            "p.fixrate, fr.ratecustomer " +
            "from fixrate fr " +
            "join project p on p.id = fr.id_project " +
            "where " +
            "p.fixrate = 1 and fr.ratecustomer is not null and " +
            "fromdate >= ? and (todate <= ? or todate is null);";

    @PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

    public InvoiceBasis getInvoiceBasis(LocalDate fromDate, LocalDate toDate) {
        InvoiceBasis invoiceBasis = InvoiceBasis.builder().
                fromDate(fromDate).
                toDate(toDate).
                invoiceCompanyBases(new ArrayList<>()).
                build();

        getInvoiceHourRateBasis(invoiceBasis, fromDate, toDate);
        Map<Long, List<InvoiceFixRateBasis>> invoiceFixRateBasisMap = getInvoiceFixRateBasis(fromDate, toDate);

        invoiceBasis.getInvoiceCompanyBases().forEach(c -> {
            List<InvoiceFixRateBasis> invoiceFixRateBases = invoiceFixRateBasisMap.get(c.getIdCompany());
            if (invoiceFixRateBases != null) {
                BigDecimal sumFixRate = invoiceFixRateBases.stream().
                        map(InvoiceFixRateBasis::getFixRate).
                        reduce(BigDecimal.ZERO, BigDecimal::add);
                c.setInvoiceFixRateBases(invoiceFixRateBasisMap.get(c.getIdCompany()));
                c.setSumFixRate(sumFixRate);
            }
        });

        return invoiceBasis;
    }

    private Map<Long, List<InvoiceFixRateBasis>> getInvoiceFixRateBasis(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(FIX_RATE_BASIS, new Object[]{fromDate, toDate});

        Map<Long, List<InvoiceFixRateBasis>> companyFixRateMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long idCompany = ((BigDecimal) row.get("idCompany")).longValue();

            List<InvoiceFixRateBasis> invoiceFixRateBases = companyFixRateMap.get(idCompany);
            if (invoiceFixRateBases == null) {
                invoiceFixRateBases = new ArrayList<>();
                companyFixRateMap.put(idCompany, invoiceFixRateBases);
            }

            InvoiceFixRateBasis invoiceFixRateBasis = InvoiceFixRateBasis.
                    builder().
                    idProject(((BigDecimal) row.get("idproject")).longValue()).
                    projectName((String) row.get("projectname")).
                    isOnCall(true).
                    fixRate(((BigDecimal) row.get("ratecustomer"))).
                    build();

            invoiceFixRateBases.add(invoiceFixRateBasis);
        }

        return companyFixRateMap;
    }

    private void getInvoiceHourRateBasis(InvoiceBasis invoiceBasis, LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(INVOICE_BASIS, new Object[]{fromDate, toDate});

        Map<Long, InvoiceCompanyBasis> companyBasisHashMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long idCompany = ((BigDecimal) row.get("idcompany")).longValue();
            InvoiceCompanyBasis invoiceCompanyBasis = companyBasisHashMap.get(idCompany);

            if (invoiceCompanyBasis == null) {
                invoiceCompanyBasis = InvoiceCompanyBasis.builder().
                        idCompany(idCompany).
                        companyName((String) row.get("companyname")).
                        hours(BigDecimal.ZERO).
                        hoursOnCall(BigDecimal.ZERO).
                        sumCustomer(BigDecimal.ZERO).
                        sumSubcontractor(BigDecimal.ZERO).
                        sumOnCall(BigDecimal.ZERO).
                        sumSubcontractorOnCall(BigDecimal.ZERO).
                        invoiceAssignmentBasis(new ArrayList<>()).
                        invoiceAssignmentBasisOnCall(new ArrayList<>()).
                        invoiceFixRateBases(new ArrayList<>()).
                        build();

                invoiceBasis.getInvoiceCompanyBases().add(invoiceCompanyBasis);
            }

            boolean isOnCall = ((BigDecimal) row.get("oncall")).intValue() == 1;
            BigDecimal totalHours = ((BigDecimal) row.get("totalTime"));

            BigDecimal customerRate = ((BigDecimal) row.get("ratecustomer"));
            BigDecimal subcontractorRate = ((BigDecimal) row.get("ratesubcontractor"));
            BigDecimal sumCustomer = (customerRate != null ? customerRate.multiply(totalHours).setScale(2, RoundingMode.HALF_UP) : null);
            BigDecimal sumSubcontractor = (subcontractorRate != null ? subcontractorRate.multiply(totalHours).setScale(2, RoundingMode.HALF_UP) : null);

            if (isOnCall) {
                BigDecimal sumOnCall = (customerRate != null ? customerRate.multiply(totalHours).setScale(2, RoundingMode.HALF_UP) : null);
                invoiceCompanyBasis.setSumOnCall(sumOnCall != null ? invoiceCompanyBasis.getSumOnCall().add(sumOnCall) : invoiceCompanyBasis.getSumOnCall());
                invoiceCompanyBasis.setHoursOnCall(invoiceCompanyBasis.getHoursOnCall().add(totalHours));
            }

            invoiceCompanyBasis.setHours(invoiceCompanyBasis.getHours().add(totalHours));
            invoiceCompanyBasis.setSumCustomer(sumCustomer != null ? invoiceCompanyBasis.getSumCustomer().add(sumCustomer) : invoiceCompanyBasis.getSumCustomer());
            invoiceCompanyBasis.setSumSubcontractor(sumSubcontractor != null ? invoiceCompanyBasis.getSumSubcontractor().add(sumSubcontractor) : invoiceCompanyBasis.getSumSubcontractor());


            String rateComment = ((String) row.get("comment"));

            String comment = "";
            if (!isOnCall && customerRate == null) {
                comment = "No rate";
            }

            InvoiceAssignmentBasis invoiceAssignmentBasis = InvoiceAssignmentBasis.
                    builder().
                    idAssignment(((BigDecimal) row.get("idassignment")).longValue()).
                    projectName((String) row.get("projectname")).
                    userName(row.get("firstname") + " " + row.get("lastname")).
                    isOnCall(isOnCall).
                    isFixRate(false).
                    hours(totalHours).
                    customerRate(customerRate).
                    subContractorRate(subcontractorRate).
                    rateComment(rateComment).
                    comment(comment).
                    sumCustomer(sumCustomer).
                    sumSubcontractor(sumSubcontractor).
                    build();

            if (isOnCall) {
                invoiceCompanyBasis.getInvoiceAssignmentBasisOnCall().add(invoiceAssignmentBasis);
            } else {
                invoiceCompanyBasis.getInvoiceAssignmentBasis().add(invoiceAssignmentBasis);
            }

            companyBasisHashMap.put(idCompany, invoiceCompanyBasis);
        }
    }
}
