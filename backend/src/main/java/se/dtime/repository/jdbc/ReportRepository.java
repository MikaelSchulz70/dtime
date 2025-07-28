package se.dtime.repository.jdbc;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import se.dtime.model.report.TaskReport;
import se.dtime.model.report.TaskUserReport;
import se.dtime.model.report.TaskUserUserReport;
import se.dtime.model.report.UserReport;

import javax.sql.DataSource;
import java.math.BigDecimal;
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
            "select u.id userId, u.firstname, u.lastname, c.id accountId, c.name accountName, p.id taskId, p.name taskName, sum(reportedtime) totalTime " +
                    "from time_report tr " +
                    "join task_contributor a on a.id = tr.id_task_contributor " +
                    "join users u on u.id = a.id_user " +
                    "join task p on p.id = a.id_task " +
                    "join account c on c.id = p.id_account " +
                    "where date >= ? and date <= ? " +
                    "{USER_CONDITION}" +
                    "group by id_task_contributor, u.id, u.firstname, u.lastname, c.id, c.name, p.id, p.name " +
                    "having sum(reportedtime) > 0 " +
                    "order by u.firstname, u.lastname, c.name, p.name";

    private final String USERS_REPORTS_NO_TIME =
            "select u.id userId, u.firstname, u.lastname " +
                    "from users u " +
                    "where u.status = 'ACTIVE' and u.id not in (" +
                    "select id_user from assignment a where a.id in (" +
                    "select id_task_contributor from time_report tr " +
                    "where " +
                    "tr.date >= ? and tr.date <= ? " +
                    "group by id_task_contributor))";

    private final String TASK_REPORT =
            "select c.id accountId, c.name accountName, p.id taskId, p.name taskName, sum(reportedtime) totalTime " +
                    "from time_report tr " +
                    "join task_contributor a on a.id = tr.id_task_contributor " +
                    "join task p on p.id = a.id_task " +
                    "join account c on c.id = p.id_account " +
                    "where date >= ? and date <= ? " +
                    "group by c.id, c.name, p.id, p.name " +
                    "having sum(reportedtime) > 0 " +
                    "order by c.name, p.name";

    private final String USER_REPORTS =
            "select u.id userId, u.firstname, u.lastname, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join task_contributor a on a.id = tr.id_task_contributor " +
                    "join users u on u.id = a.id_user " +
                    "join task p on p.id = a.id_task " +
                    "join account c on c.id = p.id_account " +
                    "where date >= ? and date <= ? " +
                    "group by u.id, u.firstname, u.lastname " +
                    "having sum(reportedtime) > 0 " +
                    "order by sum(reportedtime) desc";

    private final String TASK_USER_REPORT =
            "select c.id accountId, c.name as accountName, p.name as taskName, p.id taskId, u.id as userId, u.firstName, u.lastName, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join task_contributor a on a.id = tr.id_task_contributor " +
                    "join task p on p.id = a.id_task " +
                    "join account c on c.id = p.id_account " +
                    "join users u on a.id_user = u.id " +
                    "where date >= ? and date <= ?" +
                    "and p.internal = 1 " +
                    "group by c.id, c.name, p.id, p.name, u.id, u.firstName, u.lastName " +
                    "having sum(reportedtime) > 0 " +
                    "order by totalTime desc";

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
            long userId = ((BigDecimal) row.get("userId")).longValue();
            UserReport userReport = userReportMap.get(userId);
            if (userReport == null) {
                userReport = new UserReport();
                userReport.setUserId(userId);
                userReport.setFullName(row.get("firstname") + " " + row.get("lastname"));
                userReportMap.put(userId, userReport);
                userReports.add(userReport);
                userReport.setFromDate(fromDate);
                userReport.setToDate(toDate);
            }

            double totalTime = ((BigDecimal) row.get("totalTime")).doubleValue();
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
            taskReport.setAccountId(((BigDecimal) row.get("accountId")).longValue());
            taskReport.setAccountName((String) row.get("accountName"));
            taskReport.setTaskId(((BigDecimal) row.get("taskId")).longValue());
            taskReport.setTaskName((String) row.get("taskName"));
            taskReport.setTotalHours(((BigDecimal) row.get("totalTime")).doubleValue());
            taskReports.add(taskReport);
        }

        return taskReports;
    }

    private List<UserReport> getUserTaskReports(String sql, LocalDate fromDate, LocalDate toDate, Object[] parameters) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, parameters);

        List<UserReport> userReports = new ArrayList<>();
        Map<Long, UserReport> userReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long userId = ((BigDecimal) row.get("userId")).longValue();
            UserReport userReport = userReportMap.get(userId);
            if (userReport == null) {
                userReport = new UserReport();
                userReport.setUserId(userId);
                userReport.setFullName(row.get("firstname") + " " + row.get("lastname"));
                userReportMap.put(userId, userReport);
                userReports.add(userReport);
                userReport.setFromDate(fromDate);
                userReport.setToDate(toDate);
            }

            TaskReport taskReport = new TaskReport();
            taskReport.setAccountId(((BigDecimal) row.get("accountId")).longValue());
            taskReport.setAccountName((String) row.get("accountName"));
            taskReport.setTaskId(((BigDecimal) row.get("taskId")).longValue());
            taskReport.setTaskName((String) row.get("taskName"));
            double totalHoursTask = ((BigDecimal) row.get("totalTime")).doubleValue();
            taskReport.setTotalHours(totalHoursTask);
            userReport.getTaskReports().add(taskReport);
        }

        return userReports;
    }

    private List<UserReport> getUsersReportsNoReportedTime(String sql, LocalDate fromDate, LocalDate toDate, Object[] parameters) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, parameters);

        List<UserReport> userReports = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long userId = ((BigDecimal) row.get("userId")).longValue();
            UserReport userReport = new UserReport();
            userReport.setUserId(userId);
            userReport.setFullName(row.get("firstname") + " " + row.get("lastname"));
            userReport.setFromDate(fromDate);
            userReport.setToDate(toDate);
            userReports.add(userReport);
        }

        return userReports;
    }

    public List<TaskUserReport> getTaskUserReport(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(TASK_USER_REPORT, new Object[]{fromDate, toDate});

        List<TaskUserReport> taskUserReports = new ArrayList<>();
        Map<String, TaskUserReport> taskUserReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long accountId = ((BigDecimal) row.get("accountId")).longValue();
            long taskId = ((BigDecimal) row.get("taskId")).longValue();
            String key = accountId + "_" + taskId;
            double totalHours = ((BigDecimal) row.get("totalTime")).doubleValue();

            TaskUserReport taskUserReport = taskUserReportMap.get(key);
            if (taskUserReport == null) {
                taskUserReport = TaskUserReport.
                        builder().
                        accountId(accountId).
                        taskId(taskId).
                        accountName((String) row.get("accountName")).
                        taskName((String) row.get("taskName")).
                        fromDate(fromDate).
                        toDate(toDate).
                        taskUserUserReports(new ArrayList<>()).
                        build();

                taskUserReportMap.put(key, taskUserReport);
                taskUserReports.add(taskUserReport);
            }

            taskUserReport.setTotalHours(taskUserReport.getTotalHours() + totalHours);

            TaskUserUserReport taskUserUserReport = TaskUserUserReport.
                    builder().
                    userId(((BigDecimal) row.get("userId")).longValue()).
                    fullName(row.get("firstname") + " " + row.get("lastname")).
                    totalHours(totalHours).
                    build();

            taskUserReport.getTaskUserUserReports().add(taskUserUserReport);
        }

        return taskUserReports;
    }
}
