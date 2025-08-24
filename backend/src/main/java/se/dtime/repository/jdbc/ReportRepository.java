package se.dtime.repository.jdbc;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import se.dtime.model.report.AccountReport;
import se.dtime.model.report.TaskReport;
import se.dtime.model.report.UserReport;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository extends JdbcDaoSupport {
    @Autowired
    private DataSource dataSource;

    private final String USERS_TASK_REPORTS =
            "select u.id userId, u.firstname, u.lastname, u.email, c.id accountId, c.name accountName, p.id taskId, p.name taskName, sum(tr.reportedtime) totalTime " +
                    "from PUBLIC.time_report tr " +
                    "join PUBLIC.task_contributor a on a.id = tr.id_task_contributor " +
                    "join PUBLIC.users u on u.id = a.id_user " +
                    "join PUBLIC.task p on p.id = a.id_task " +
                    "join PUBLIC.account c on c.id = p.id_account " +
                    "where tr.date >= ? and tr.date <= ? " +
                    "{USER_CONDITION}" +
                    "group by tr.id_task_contributor, u.id, u.firstname, u.lastname, u.email, c.id, c.name, p.id, p.name " +
                    "having sum(tr.reportedtime) > 0 " +
                    "order by u.firstname, u.lastname, c.name, p.name";

    private final String USERS_REPORTS_NO_TIME =
            "select u.id userId, u.firstname, u.lastname, u.email " +
                    "from PUBLIC.users u " +
                    "where u.status = 'ACTIVE' and u.id not in (" +
                    "select id_user from PUBLIC.task_contributor a where a.id in (" +
                    "select id_task_contributor from PUBLIC.time_report tr " +
                    "where " +
                    "tr.date >= ? and tr.date <= ? " +
                    "group by id_task_contributor))";

    private final String TASK_REPORT =
            "select c.id accountId, c.name accountName, p.id taskId, p.name taskName, sum(tr.reportedtime) totalTime " +
                    "from PUBLIC.time_report tr " +
                    "join PUBLIC.task_contributor a on a.id = tr.id_task_contributor " +
                    "join PUBLIC.task p on p.id = a.id_task " +
                    "join PUBLIC.account c on c.id = p.id_account " +
                    "where tr.date >= ? and tr.date <= ? " +
                    "group by c.id, c.name, p.id, p.name " +
                    "having sum(tr.reportedtime) > 0 " +
                    "order by c.name, p.name";

    private final String ACCOUNT_REPORT =
            "select c.id accountId, c.name accountName, sum(tr.reportedtime) totalTime " +
                    "from PUBLIC.time_report tr " +
                    "join PUBLIC.task_contributor a on a.id = tr.id_task_contributor " +
                    "join PUBLIC.task p on p.id = a.id_task " +
                    "join PUBLIC.account c on c.id = p.id_account " +
                    "where tr.date >= ? and tr.date <= ? " +
                    "group by c.id, c.name " +
                    "having sum(tr.reportedtime) > 0 " +
                    "order by c.name";

    private final String USER_REPORTS =
            "select u.id userId, u.firstname, u.lastname, u.email, sum(tr.reportedtime) totalTime " +
                    "from PUBLIC.time_report tr " +
                    "join PUBLIC.task_contributor a on a.id = tr.id_task_contributor " +
                    "join PUBLIC.users u on u.id = a.id_user " +
                    "join PUBLIC.task p on p.id = a.id_task " +
                    "join PUBLIC.account c on c.id = p.id_account " +
                    "where tr.date >= ? and tr.date <= ? " +
                    "group by u.id, u.firstname, u.lastname, u.email " +
                    "having sum(tr.reportedtime) > 0 " +
                    "order by sum(tr.reportedtime) desc";

    @PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

    public List<UserReport> getUserTaskReports(LocalDate fromDate, LocalDate toDate) {
        String sql = USERS_TASK_REPORTS.replace("{USER_CONDITION}", "");
        List<UserReport> userReports = getUserTaskReports(sql, fromDate, toDate, new Object[]{fromDate, toDate});

        // Fetch user reports for users that have no reported time
        List<UserReport> userReportsNoTime = getUsersReportsNoReportedTime(USERS_REPORTS_NO_TIME, fromDate, toDate, new Object[]{fromDate, toDate});
        userReports.addAll(userReportsNoTime);

        return userReports;
    }

    public List<UserReport> getUserTaskReports(long userId, LocalDate fromDate, LocalDate toDate) {
        String sql = USERS_TASK_REPORTS.replace("{USER_CONDITION}", "and u.id = ? ");
        return getUserTaskReports(sql, fromDate, toDate, new Object[]{fromDate, toDate, userId});
    }

    public List<UserReport> getUserReports(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(USER_REPORTS, new Object[]{fromDate, toDate});

        List<UserReport> userReports = new ArrayList<>();
        Map<Long, UserReport> userReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long userId = (Long) row.get("userId");
            UserReport userReport = userReportMap.get(userId);
            if (userReport == null) {
                userReport = new UserReport();
                userReport.setUserId(userId);
                userReport.setFullName(row.get("firstname") + " " + row.get("lastname"));
                userReport.setEmail((String) row.get("email"));
                userReportMap.put(userId, userReport);
                userReports.add(userReport);
                userReport.setFromDate(fromDate);
                userReport.setToDate(toDate);
            }

            double totalTime = ((Number) row.get("totalTime")).floatValue();
            userReport.setTotalTime(totalTime);
        }

        // Fetch user reports for users that have no reported time
        List<UserReport> userReportsNoTime = getUsersReportsNoReportedTime(USERS_REPORTS_NO_TIME, fromDate, toDate, new Object[]{fromDate, toDate});
        userReports.addAll(userReportsNoTime);

        return userReports;
    }

    public List<TaskReport> getTaskReports(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(TASK_REPORT, new Object[]{fromDate, toDate});

        List<TaskReport> taskReports = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            TaskReport taskReport = new TaskReport();
            taskReport.setAccountId((Long) row.get("accountId"));
            taskReport.setAccountName((String) row.get("accountName"));
            taskReport.setTaskId((Long) row.get("taskId"));
            taskReport.setTaskName((String) row.get("taskName"));
            taskReport.setTotalHours(((Number) row.get("totalTime")).floatValue());
            taskReports.add(taskReport);
        }

        return taskReports;
    }

    private List<UserReport> getUserTaskReports(String sql, LocalDate fromDate, LocalDate toDate, Object[] parameters) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, parameters);

        List<UserReport> userReports = new ArrayList<>();
        Map<Long, UserReport> userReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long userId = (Long) row.get("userId");
            UserReport userReport = userReportMap.get(userId);
            if (userReport == null) {
                userReport = new UserReport();
                userReport.setUserId(userId);
                userReport.setFullName(row.get("firstname") + " " + row.get("lastname"));
                userReport.setEmail((String) row.get("email"));
                userReportMap.put(userId, userReport);
                userReports.add(userReport);
                userReport.setFromDate(fromDate);
                userReport.setToDate(toDate);
            }

            TaskReport taskReport = new TaskReport();
            taskReport.setAccountId((Long) row.get("accountId"));
            taskReport.setAccountName((String) row.get("accountName"));
            taskReport.setTaskId((Long) row.get("taskId"));
            taskReport.setTaskName((String) row.get("taskName"));
            double totalHoursTask = ((Number) row.get("totalTime")).floatValue();
            taskReport.setTotalHours(totalHoursTask);
            userReport.getTaskReports().add(taskReport);
            userReport.setTotalTime(userReport.getTotalTime() + totalHoursTask);
        }

        return userReports;
    }

    private List<UserReport> getUsersReportsNoReportedTime(String sql, LocalDate fromDate, LocalDate toDate, Object[] parameters) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, parameters);

        List<UserReport> userReports = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long userId = (Long) row.get("userId");
            UserReport userReport = new UserReport();
            userReport.setUserId(userId);
            userReport.setFullName(row.get("firstname") + " " + row.get("lastname"));
            userReport.setEmail((String) row.get("email"));
            userReport.setFromDate(fromDate);
            userReport.setToDate(toDate);
            userReports.add(userReport);
        }

        return userReports;
    }

    public List<AccountReport> getAccountReports(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(ACCOUNT_REPORT, new Object[]{fromDate, toDate});

        List<AccountReport> accountReports = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            AccountReport accountReport = new AccountReport();
            accountReport.setAccountId((Long) row.get("accountId"));
            accountReport.setAccountName((String) row.get("accountName"));
            accountReport.setTotalHours(((Number) row.get("totalTime")).floatValue());
            accountReports.add(accountReport);
        }

        return accountReports;
    }
}
