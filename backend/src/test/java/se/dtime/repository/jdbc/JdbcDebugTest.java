package se.dtime.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.*;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;
import se.dtime.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@Import(ReportRepository.class)
class JdbcDebugTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskContributorRepository taskContributorRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void debugDataAndQueries() {
        // Create test data
        LocalDate fromDate = LocalDate.of(2024, 12, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);

        // Create users
        UserPO user1 = createAndSaveUser("alice@example.com", "Alice", "Smith");
        System.out.println("Created user1 with ID: " + user1.getId());

        // Create accounts
        AccountPO account1 = createAndSaveAccount("Account Alpha");
        System.out.println("Created account1 with ID: " + account1.getId());

        // Create tasks
        TaskPO task1 = createAndSaveTask("Task A", account1);
        System.out.println("Created task1 with ID: " + task1.getId());

        // Create task contributors
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user1, task1);
        System.out.println("Created contributor1 with ID: " + contributor1.getId());

        // Create time entries
        TimeEntryPO timeEntry = createTimeEntry(contributor1, LocalDate.of(2024, 12, 15), 8.0f);
        System.out.println("Created timeEntry with ID: " + timeEntry.getId());

        // Check actual data in tables
        System.out.println("\n=== Data in time_report ===");
        List<Map<String, Object>> timeReports = jdbcTemplate.queryForList("SELECT * FROM \"PUBLIC\".\"time_report\"");
        System.out.println("Found " + timeReports.size() + " records:");
        for (Map<String, Object> row : timeReports) {
            System.out.println("  " + row);
        }

        System.out.println("\n=== Data in users ===");
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT \"id\", \"firstname\", \"lastname\", \"status\" FROM \"PUBLIC\".\"users\"");
        System.out.println("Found " + users.size() + " records:");
        for (Map<String, Object> row : users) {
            System.out.println("  " + row);
        }

        System.out.println("\n=== Data in task_contributor ===");
        List<Map<String, Object>> contributors = jdbcTemplate.queryForList("SELECT * FROM \"PUBLIC\".\"task_contributor\"");
        System.out.println("Found " + contributors.size() + " records:");
        for (Map<String, Object> row : contributors) {
            System.out.println("  " + row);
        }

        // Test the actual JDBC query being used
        System.out.println("\n=== Testing JDBC query ===");
        String testQuery = "select u.\"id\" userId, u.\"firstname\", u.\"lastname\", sum(tr.\"reportedtime\") totalTime " +
                "from \"PUBLIC\".\"time_report\" tr " +
                "join \"PUBLIC\".\"task_contributor\" a on a.\"id\" = tr.\"id_task_contributor\" " +
                "join \"PUBLIC\".\"users\" u on u.\"id\" = a.\"id_user\" " +
                "join \"PUBLIC\".\"task\" p on p.\"id\" = a.\"id_task\" " +
                "join \"PUBLIC\".\"account\" c on c.\"id\" = p.\"id_account\" " +
                "where tr.\"date\" >= ? and tr.\"date\" <= ? " +
                "group by u.\"id\", u.\"firstname\", u.\"lastname\" " +
                "having sum(tr.\"reportedtime\") > 0 " +
                "order by sum(tr.\"reportedtime\") desc";

        List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(testQuery, fromDate, toDate);
        System.out.println("JDBC query returned " + queryResults.size() + " records:");
        for (Map<String, Object> row : queryResults) {
            System.out.println("  " + row);
        }

        // Test simplified query
        System.out.println("\n=== Testing simplified query ===");
        String simpleQuery = "SELECT tr.\"date\", tr.\"reportedtime\", u.\"firstname\", u.\"lastname\" " +
                "FROM \"PUBLIC\".\"time_report\" tr " +
                "JOIN \"PUBLIC\".\"task_contributor\" a ON a.\"id\" = tr.\"id_task_contributor\" " +
                "JOIN \"PUBLIC\".\"users\" u ON u.\"id\" = a.\"id_user\"";

        List<Map<String, Object>> simpleResults = jdbcTemplate.queryForList(simpleQuery);
        System.out.println("Simple query returned " + simpleResults.size() + " records:");
        for (Map<String, Object> row : simpleResults) {
            System.out.println("  " + row);
        }

        // Test ReportRepository
        System.out.println("\n=== Testing ReportRepository.getUserReports ===");
        try {
            var userReports = reportRepository.getUserReports(fromDate, toDate);
            System.out.println("ReportRepository returned " + userReports.size() + " reports");
        } catch (Exception e) {
            System.out.println("ReportRepository failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UserPO createAndSaveUser(String email, String firstName, String lastName) {
        UserPO user = new UserPO();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("password123");
        user.setUserRole(UserRole.USER);
        user.setActivationStatus(ActivationStatus.ACTIVE);
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        user.setCreateDateTime(LocalDateTime.now());
        user.setUpdatedDateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    private AccountPO createAndSaveAccount(String name) {
        AccountPO account = new AccountPO();
        account.setName(name);
        account.setActivationStatus(ActivationStatus.ACTIVE);
        account.setCreatedBy(1L);
        account.setUpdatedBy(1L);
        account.setCreateDateTime(LocalDateTime.now());
        account.setUpdatedDateTime(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private TaskPO createAndSaveTask(String name, AccountPO account) {
        TaskPO task = new TaskPO();
        task.setName(name);
        task.setActivationStatus(ActivationStatus.ACTIVE);
        task.setAccount(account);
        task.setCreatedBy(1L);
        task.setUpdatedBy(1L);
        task.setCreateDateTime(LocalDateTime.now());
        task.setUpdatedDateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    private TaskContributorPO createAndSaveTaskContributor(UserPO user, TaskPO task) {
        TaskContributorPO contributor = new TaskContributorPO();
        contributor.setUser(user);
        contributor.setTask(task);
        contributor.setActivationStatus(ActivationStatus.ACTIVE);
        contributor.setCreatedBy(1L);
        contributor.setUpdatedBy(1L);
        contributor.setCreateDateTime(LocalDateTime.now());
        contributor.setUpdatedDateTime(LocalDateTime.now());
        return taskContributorRepository.save(contributor);
    }

    private TimeEntryPO createTimeEntry(TaskContributorPO contributor, LocalDate date, float time) {
        TimeEntryPO timeEntry = new TimeEntryPO();
        timeEntry.setTaskContributor(contributor);
        timeEntry.setDate(date);
        timeEntry.setTime(time);
        timeEntry.setCreatedBy(1L);
        timeEntry.setUpdatedBy(1L);
        timeEntry.setCreateDateTime(LocalDateTime.now());
        timeEntry.setUpdatedDateTime(LocalDateTime.now());
        return timeEntryRepository.save(timeEntry);
    }
}