package se.dtime.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import se.dtime.model.ProjectCategory;
import se.dtime.model.report.FollowUpData;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class FollowUpReportRepository extends JdbcDaoSupport {
    @Autowired
    private DataSource dataSource;

    private final String FOLLOW_UP =
            "select c.id idcompany, c.name as companyname, p.name as projectname, p.id idproject, p.oncall, p.category," +
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
                    "group by c.id, c.name, p.id, p.name, p.oncall, p.category, a.id, u.id, u.firstName, u.lastName, r.ratecustomer, r.ratesubcontractor, r.comment " +
                    "having sum(reportedtime) > 0 " +
                    "order by c.name, p.name, p.oncall desc;";

    @PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

    public List<FollowUpData> getFollowUpData(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(FOLLOW_UP, new Object[]{fromDate, toDate});

        List<FollowUpData> followUpDataList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            BigDecimal totalTime = ((BigDecimal) row.get("totalTime"));
            BigDecimal customerRate = ((BigDecimal) row.get("ratecustomer"));
            BigDecimal subcontractorRate = ((BigDecimal) row.get("ratesubcontractor"));
            BigDecimal amount = null;
            BigDecimal amountSubcontractor = null;

            amount = (customerRate != null ? customerRate.multiply(totalTime).setScale(2, RoundingMode.HALF_UP) : null);
            amountSubcontractor = (subcontractorRate != null ? subcontractorRate.multiply(totalTime).setScale(2, RoundingMode.HALF_UP) : null);

            FollowUpData followUpData = FollowUpData.
                    builder().
                    idCompany(((BigDecimal) row.get("idcompany")).longValue()).
                    companyName((String) row.get("companyname")).
                    idProject(((BigDecimal) row.get("idproject")).longValue()).
                    projectName((String) row.get("projectname")).
                    idUser(((BigDecimal) row.get("iduser")).longValue()).
                    userName(row.get("firstname") + " " + row.get("lastname")).
                    isOncall((((BigDecimal) row.get("oncall")).intValue() == 1)).
                    category(ProjectCategory.valueOf((String) row.get("category"))).
                    totalTime(totalTime).
                    rateCustomer(customerRate).
                    rateSubcontractor(subcontractorRate).
                    amount(amount).
                    amountSubcontractor(amountSubcontractor).
                    build();

            followUpDataList.add(followUpData);
        }

        return followUpDataList;
    }
}
