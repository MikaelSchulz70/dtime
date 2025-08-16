package se.dtime.repository.jdbc;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import se.dtime.model.report.FollowUpData;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class FollowUpReportRepository extends JdbcDaoSupport {
    @Autowired
    private DataSource dataSource;

    private final String FOLLOW_UP =
            "select c.\"id\" accountId, c.\"name\" as accountName, p.\"name\" as taskName, p.\"id\" taskId, " +
                    "a.\"id\" taskContributorId, u.\"id\" as userId, u.\"firstname\", u.\"lastname\", sum(tr.\"reportedtime\") totalTime, " +
                    "r.comment " +
                    "from \"PUBLIC\".\"time_report\" tr " +
                    "join \"PUBLIC\".\"task_contributor\" a on a.\"id\" = tr.\"id_task_contributor\" " +
                    "join \"PUBLIC\".\"task\" p on p.\"id\" = a.\"id_task\" " +
                    "join \"PUBLIC\".\"account\" c on c.\"id\" = p.\"id_account\" " +
                    "join \"PUBLIC\".\"users\" u on a.\"id_user\" = u.\"id\" " +
                    "left join rate r on r.id_task_contributor = a.\"id\" and (tr.\"date\" >= r.fromdate and (tr.\"date\" <= r.todate or r.todate is null)) " +
                    "where tr.\"date\" >= ? and tr.\"date\" <= ? " +
                    "group by c.\"id\", c.\"name\", p.\"id\", p.\"name\", a.\"id\", u.\"id\", u.\"firstname\", u.\"lastname\", r.comment " +
                    "having sum(tr.\"reportedtime\") > 0 " +
                    "order by c.\"name\", p.\"name\" desc;";

    @PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

    public List<FollowUpData> getFollowUpData(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(FOLLOW_UP, new Object[]{fromDate, toDate});

        List<FollowUpData> followUpDataList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            double totalTime = ((Number) row.get("totalTime")).doubleValue();

            FollowUpData followUpData = FollowUpData.
                    builder().
                    accountId((Long) row.get("accountId")).
                    accountName((String) row.get("accountName")).
                    taskId((Long) row.get("taskId")).
                    taskName((String) row.get("taskName")).
                    userId((Long) row.get("userId")).
                    fullName(row.get("firstname") + " " + row.get("lastname")).
                    totalTime(BigDecimal.valueOf(totalTime)).
                    build();

            followUpDataList.add(followUpData);
        }

        return followUpDataList;
    }
}
