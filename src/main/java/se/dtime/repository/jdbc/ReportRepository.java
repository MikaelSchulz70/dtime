package se.dtime.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import se.dtime.model.ProjectCategory;
import se.dtime.model.UserCategory;
import se.dtime.model.report.*;

import javax.annotation.PostConstruct;
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

    private final String USERS_PROJECT_REPORTS =
            "select u.id idUser, u.firstname, u.lastname, u.category, c.id idCompany, c.name companyName, p.id idProject, p.name projectName, p.provision, p.internal, p.oncall, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join assignment a on a.id = tr.id_assignment " +
                    "join users u on u.id = a.id_user " +
                    "join project p on p.id = a.id_project " +
                    "join company c on c.id = p.id_company " +
                    "where date >= ? and date <= ? " +
                    "{USER_CONDITION}" +
                    "group by id_assignment, u.id, u.firstname, u.lastname, u.category, c.id, c.name, p.id, p.name, p.provision, p.oncall " +
                    "having sum(reportedtime) > 0 " +
                    "order by u.firstname, u.lastname, c.name, p.name, p.provision";

    private final String USERS_REPORTS_NO_TIME =
            "select u.id idUser, u.firstname, u.lastname, u.category from users u " +
                    "where u.status = 'ACTIVE' and u.id not in (" +
                    "select id_user from assignment a where a.id in (" +
                    "select id_assignment from timereport tr " +
                    "where " +
                    "tr.date >= ? and tr.date <= ? " +
                    "group by id_assignment))";

    private final String PROJECT_REPORT =
            "select c.id idCompany, c.name companyName, p.id idProject, p.name projectName, p.provision, p.internal, p.oncall, p.category, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join assignment a on a.id = tr.id_assignment " +
                    "join project p on p.id = a.id_project " +
                    "join company c on c.id = p.id_company " +
                    "where date >= ? and date <= ? " +
                    "group by c.id, c.name, p.id, p.name, p.provision, p.internal, p.oncall, p.category " +
                    "having sum(reportedtime) > 0 " +
                    "order by c.name, p.name, p.provision";

    private final String USER_REPORTS =
            "select u.id idUser, u.firstname, u.lastname, p.provision, p.internal, p.oncall, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join assignment a on a.id = tr.id_assignment " +
                    "join users u on u.id = a.id_user " +
                    "join project p on p.id = a.id_project " +
                    "join company c on c.id = p.id_company " +
                    "where date >= ? and date <= ? " +
                    "group by u.id, u.firstname, u.lastname, p.provision, p.internal, p.oncall " +
                    "having sum(reportedtime) > 0 " +
                    "order by sum(reportedtime) desc, p.provision, p.internal";

    private final String PROJECT_CATEGORY_REPORT =
            "select  p.category, p.provision, p.internal, p.oncall, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join assignment a on a.id = tr.id_assignment " +
                    "join project p on p.id = a.id_project " +
                    "join company c on c.id = p.id_company " +
                    "where date >= ? and date <= ? " +
                    "group by p.category, p.provision, p.internal, p.oncall " +
                    "having sum(reportedtime) > 0 " +
                    "order by totalTime desc";

    private final String PROJECT_USER_REPORT =
            "select c.id idcompany, c.name as companyname, p.name as projectname, p.id idproject, u.id as iduser, u.firstName, u.lastName, sum(reportedtime) totalTime " +
                    "from timereport tr " +
                    "join assignment a on a.id = tr.id_assignment " +
                    "join project p on p.id = a.id_project " +
                    "join company c on c.id = p.id_company " +
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

    public List<UserReport> getUserProjectReports(LocalDate fromDate, LocalDate toDate) {
        String sql = USERS_PROJECT_REPORTS.replace("{USER_CONDITION}", "");
        List<UserReport> userReports = getUserProjectReports(sql, fromDate, toDate, new Object[]{fromDate, toDate});

        // Fetch user reports for users that have no reported time
        List<UserReport> userReportsNoTime = getUsersReportsNoReportedTime(USERS_REPORTS_NO_TIME, fromDate, toDate, new Object[]{fromDate, toDate});
        userReports.addAll(userReportsNoTime);

        return userReports;
    }

    public List<UserReport> getUserProjectReports(long idUser, LocalDate fromDate, LocalDate toDate) {
        String sql = USERS_PROJECT_REPORTS.replace("{USER_CONDITION}", "and u.id = ? ");
        return getUserProjectReports(sql, fromDate, toDate, new Object[]{fromDate, toDate, idUser});
    }

    public List<UserReport> getUserReports(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(USER_REPORTS, new Object[]{fromDate, toDate});

        List<UserReport> userReports = new ArrayList<>();
        Map<Long, UserReport> userReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long idUser = ((BigDecimal) row.get("idUser")).longValue();
            UserReport userReport = userReportMap.get(idUser);
            if (userReport == null) {
                userReport = new UserReport();
                userReport.setIdUser(idUser);
                userReport.setUserName(row.get("firstname") + " " + row.get("lastname"));
                userReportMap.put(idUser, userReport);
                userReports.add(userReport);
                userReport.setFromDate(fromDate);
                userReport.setToDate(toDate);
            }

            boolean isProvision = (((BigDecimal) row.get("provision")).intValue() == 1);
            boolean isInternal = (((BigDecimal) row.get("internal")).intValue() == 1);
            boolean isOnCall = (((BigDecimal) row.get("oncall")).intValue() == 1);
            double totalTime = ((BigDecimal) row.get("totalTime")).doubleValue();

            if (isOnCall) {
                userReport.setTotalTimeOnCall(totalTime);
            } else {
                if (isProvision) {
                    if (isInternal) {
                        userReport.setTotalTimeInternalProvision(totalTime);
                    } else {
                        userReport.setTotalTimeExternalProvision(totalTime);
                    }
                } else {
                    if (isInternal) {
                        userReport.setTotalTimeInternalNoProvision(totalTime);
                    } else {
                        userReport.setTotalTimeExternalNoProvision(totalTime);
                    }
                }
            }
        }

        // Fetch user reports for users that have no reported time
        List<UserReport> userReportsNoTime = getUsersReportsNoReportedTime(USERS_REPORTS_NO_TIME, fromDate, toDate, new Object[]{fromDate, toDate});
        userReports.addAll(userReportsNoTime);

        return userReports;
    }

    public List<ProjectReport> getProjectsReports(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(PROJECT_REPORT, new Object[]{fromDate, toDate});

        List<ProjectReport> projectReports = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            ProjectReport projectReport = new ProjectReport();
            projectReport.setIdCompany(((BigDecimal) row.get("idCompany")).longValue());
            projectReport.setCompanyName((String) row.get("companyName"));
            projectReport.setIdProject(((BigDecimal) row.get("idProject")).longValue());
            projectReport.setProjectName((String) row.get("projectName"));
            projectReport.setTotalHours(((BigDecimal) row.get("totalTime")).doubleValue());
            boolean isProvision = ((BigDecimal) row.get("provision")).intValue() == 1;
            projectReport.setProvision(isProvision);
            boolean isInternal = ((BigDecimal) row.get("internal")).intValue() == 1;
            projectReport.setInternal(isInternal);
            boolean isOnCall = ((BigDecimal) row.get("oncall")).intValue() == 1;
            projectReport.setOnCall(isOnCall);

            String category = (String) row.get("category");
            ProjectCategory projectCategory = ProjectCategory.valueOf(category);
            projectReport.setProjectCategory(projectCategory);

            projectReports.add(projectReport);
        }

        return projectReports;
    }

    private List<UserReport> getUserProjectReports(String sql, LocalDate fromDate, LocalDate toDate, Object[] parameters) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, parameters);

        List<UserReport> userReports = new ArrayList<>();
        Map<Long, UserReport> userReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long idUser = ((BigDecimal) row.get("idUser")).longValue();
            UserReport userReport = userReportMap.get(idUser);
            if (userReport == null) {
                userReport = new UserReport();
                userReport.setIdUser(idUser);
                userReport.setUserName(row.get("firstname") + " " + row.get("lastname"));
                userReportMap.put(idUser, userReport);
                userReports.add(userReport);
                userReport.setFromDate(fromDate);
                userReport.setToDate(toDate);
                userReport.setUserCategory(UserCategory.valueOf((String) row.get("category")));
            }

            ProjectReport projectReport = new ProjectReport();
            projectReport.setIdCompany(((BigDecimal) row.get("idCompany")).longValue());
            projectReport.setCompanyName((String) row.get("companyName"));
            projectReport.setIdProject(((BigDecimal) row.get("idProject")).longValue());
            projectReport.setProjectName((String) row.get("projectName"));
            double totalHoursProject = ((BigDecimal) row.get("totalTime")).doubleValue();
            projectReport.setTotalHours(totalHoursProject);

            boolean isProvision = ((BigDecimal) row.get("provision")).intValue() == 1;
            projectReport.setProvision(isProvision);

            boolean isInternal = ((BigDecimal) row.get("internal")).intValue() == 1;
            projectReport.setInternal(isInternal);

            boolean isOnCall = ((BigDecimal) row.get("oncall")).intValue() == 1;
            projectReport.setOnCall(isOnCall);

            updateUserTime(userReport, totalHoursProject, isProvision, isInternal, isOnCall);

            userReport.getProjectReports().add(projectReport);
        }

        return userReports;
    }

    private void updateUserTime(UserReport userReport, double totalHoursProject, boolean isProvision, boolean isInternal, boolean isOnCall) {
        if (isOnCall) {
            userReport.setTotalTimeOnCall(userReport.getTotalTimeOnCall() + totalHoursProject);
        } else {
            if (isProvision) {
                if (isInternal) {
                    userReport.setTotalTimeInternalProvision(userReport.getTotalTimeInternalProvision() + totalHoursProject);
                } else {
                    userReport.setTotalTimeExternalProvision(userReport.getTotalTimeExternalProvision() + totalHoursProject);
                }
            } else {
                if (isInternal) {
                    userReport.setTotalTimeInternalNoProvision(userReport.getTotalTimeInternalNoProvision() + totalHoursProject);
                } else {
                    userReport.setTotalTimeExternalNoProvision(userReport.getTotalTimeExternalNoProvision() + totalHoursProject);
                }
            }
        }
    }

    private List<UserReport> getUsersReportsNoReportedTime(String sql, LocalDate fromDate, LocalDate toDate, Object[] parameters) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, parameters);

        List<UserReport> userReports = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long idUser = ((BigDecimal) row.get("idUser")).longValue();
            UserReport userReport = new UserReport();
            userReport.setIdUser(idUser);
            userReport.setUserName(row.get("firstname") + " " + row.get("lastname"));
            userReport.setFromDate(fromDate);
            userReport.setToDate(toDate);
            userReport.setUserCategory(UserCategory.valueOf((String) row.get("category")));
            userReports.add(userReport);
        }

        return userReports;
    }

    public List<ProjectCategoryReport> getProjectsCategoryReports(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(PROJECT_CATEGORY_REPORT, new Object[]{fromDate, toDate});

        List<ProjectCategoryReport> projectCategoryReports = new ArrayList<>();
        Map<ProjectCategory, ProjectCategoryReport> projectCategoryReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            ProjectSubCategoryReport projectSubCategoryReport = new ProjectSubCategoryReport();
            double totalHours = ((BigDecimal) row.get("totalTime")).doubleValue();
            projectSubCategoryReport.setTotalHours(totalHours);
            boolean isProvision = ((BigDecimal) row.get("provision")).intValue() == 1;
            projectSubCategoryReport.setProvision(isProvision);
            boolean isInternal = ((BigDecimal) row.get("internal")).intValue() == 1;
            projectSubCategoryReport.setInternal(isInternal);
            boolean isOnCall = ((BigDecimal) row.get("oncall")).intValue() == 1;
            projectSubCategoryReport.setOnCall(isOnCall);

            String category = (String) row.get("category");
            ProjectCategory projectCategory = ProjectCategory.valueOf(category);

            ProjectCategoryReport projectCategoryReport = projectCategoryReportMap.get(projectCategory);
            if (projectCategoryReport == null) {
                projectCategoryReport = new ProjectCategoryReport();
                projectCategoryReport.setProjectCategory(projectCategory);
                projectCategoryReport.setProjectSubCategoryReports(new ArrayList<>());
                projectCategoryReportMap.put(projectCategory, projectCategoryReport);
                projectCategoryReports.add(projectCategoryReport);
            }

            projectCategoryReport.setTotalHours(projectCategoryReport.getTotalHours() + totalHours);
            projectCategoryReport.getProjectSubCategoryReports().add(projectSubCategoryReport);
        }

        return projectCategoryReports;
    }

    public List<ProjectUserReport> getProjectUserReport(LocalDate fromDate, LocalDate toDate) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(PROJECT_USER_REPORT, new Object[]{fromDate, toDate});

        List<ProjectUserReport> projectUserReports = new ArrayList<>();
        Map<String, ProjectUserReport> projectUserReportMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            long idCompany = ((BigDecimal) row.get("idcompany")).longValue();
            long idProject = ((BigDecimal) row.get("idproject")).longValue();
            String key = idCompany + "_" + idProject;
            double totalHours = ((BigDecimal) row.get("totalTime")).doubleValue();

            ProjectUserReport projectUserReport = projectUserReportMap.get(key);
            if (projectUserReport == null) {
                projectUserReport = ProjectUserReport.
                        builder().
                        idCompany(idCompany).
                        idProject(idProject).
                        companyName((String) row.get("companyname")).
                        projectName((String) row.get("projectname")).
                        fromDate(fromDate).
                        toDate(toDate).
                        projectUserUserReports(new ArrayList<>()).
                        build();

                projectUserReportMap.put(key, projectUserReport);
                projectUserReports.add(projectUserReport);
            }

            projectUserReport.setTotalHours(projectUserReport.getTotalHours() + totalHours);

            ProjectUserUserReport projectUserUserReport = ProjectUserUserReport.
                    builder().
                    idUser(((BigDecimal) row.get("iduser")).longValue()).
                    userName(row.get("firstname") + " " + row.get("lastname")).
                    totalHours(totalHours).
                    build();

            projectUserReport.getProjectUserUserReports().add(projectUserUserReport);
        }

        return projectUserReports;
    }
}
