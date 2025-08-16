package se.dtime.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.dbmodel.*;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;
import se.dtime.model.report.AccountReport;
import se.dtime.model.report.TaskReport;
import se.dtime.model.report.UserReport;
import se.dtime.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@Import(ReportRepository.class)
@Transactional
class ReportRepositoryIT {

    @PersistenceContext
    private EntityManager entityManager;

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

    private UserPO user1, user2, user3;
    private AccountPO account1, account2;
    private TaskPO task1, task2, task3;
    private TaskContributorPO contributor1, contributor2, contributor3, contributor4;
    private LocalDate fromDate, toDate;

    @BeforeEach
    void setUp() {
        // Create test data
        fromDate = LocalDate.of(2024, 12, 1);
        toDate = LocalDate.of(2024, 12, 31);

        // Create users
        user1 = createAndSaveUser("alice@example.com", "Alice", "Smith");
        user2 = createAndSaveUser("bob@example.com", "Bob", "Johnson");
        user3 = createAndSaveUser("charlie@example.com", "Charlie", "Brown");

        // Create accounts
        account1 = createAndSaveAccount("Account Alpha");
        account2 = createAndSaveAccount("Account Beta");

        // Create tasks
        task1 = createAndSaveTask("Task A", account1);
        task2 = createAndSaveTask("Task B", account1);
        task3 = createAndSaveTask("Task C", account2);

        // Create task contributors
        contributor1 = createAndSaveTaskContributor(user1, task1);
        contributor2 = createAndSaveTaskContributor(user1, task2);
        contributor3 = createAndSaveTaskContributor(user2, task1);
        contributor4 = createAndSaveTaskContributor(user2, task3);

        // Create time entries
        createTimeEntry(contributor1, LocalDate.of(2024, 12, 15), 8.0f);
        createTimeEntry(contributor1, LocalDate.of(2024, 12, 16), 7.5f);
        createTimeEntry(contributor2, LocalDate.of(2024, 12, 17), 6.0f);
        createTimeEntry(contributor3, LocalDate.of(2024, 12, 18), 8.5f);
        createTimeEntry(contributor4, LocalDate.of(2024, 12, 19), 5.0f);

        // Time entry outside date range
        createTimeEntry(contributor1, LocalDate.of(2024, 11, 30), 4.0f);
        
        // Flush and clear to ensure data is committed to database for JDBC queries
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldGetUserTaskReports() {
        List<UserReport> reports = reportRepository.getUserTaskReports(fromDate, toDate);

        assertThat(reports).hasSize(3); // 2 users with time + 1 user without time
        
        // Check user with task reports (Alice)
        UserReport aliceReport = reports.stream()
                .filter(r -> "Alice Smith".equals(r.getFullName()))
                .findFirst()
                .orElse(null);
        
        assertThat(aliceReport).isNotNull();
        assertThat(aliceReport.getUserId()).isEqualTo(user1.getId());
        assertThat(aliceReport.getTotalTime()).isEqualTo(21.5); // 8.0 + 7.5 + 6.0
        assertThat(aliceReport.getTaskReports()).hasSize(2);
        assertThat(aliceReport.getFromDate()).isEqualTo(fromDate);
        assertThat(aliceReport.getToDate()).isEqualTo(toDate);

        // Check user with task reports (Bob)
        UserReport bobReport = reports.stream()
                .filter(r -> "Bob Johnson".equals(r.getFullName()))
                .findFirst()
                .orElse(null);
        
        assertThat(bobReport).isNotNull();
        assertThat(bobReport.getUserId()).isEqualTo(user2.getId());
        assertThat(bobReport.getTotalTime()).isEqualTo(13.5); // 8.5 + 5.0

        // Check user without time reports (Charlie)
        UserReport charlieReport = reports.stream()
                .filter(r -> "Charlie Brown".equals(r.getFullName()))
                .findFirst()
                .orElse(null);
        
        assertThat(charlieReport).isNotNull();
        assertThat(charlieReport.getUserId()).isEqualTo(user3.getId());
        assertThat(charlieReport.getTotalTime()).isEqualTo(0.0);
        assertThat(charlieReport.getTaskReports()).isEmpty();
    }

    @Test
    void shouldGetUserTaskReportsForSpecificUser() {
        List<UserReport> reports = reportRepository.getUserTaskReports(user1.getId(), fromDate, toDate);

        assertThat(reports).hasSize(1);
        
        UserReport aliceReport = reports.get(0);
        assertThat(aliceReport.getUserId()).isEqualTo(user1.getId());
        assertThat(aliceReport.getFullName()).isEqualTo("Alice Smith");
        assertThat(aliceReport.getTotalTime()).isEqualTo(21.5);
        assertThat(aliceReport.getTaskReports()).hasSize(2);

        // Check task reports
        TaskReport taskAReport = aliceReport.getTaskReports().stream()
                .filter(tr -> "Task A".equals(tr.getTaskName()))
                .findFirst()
                .orElse(null);
        
        assertThat(taskAReport).isNotNull();
        assertThat(taskAReport.getAccountId()).isEqualTo(account1.getId());
        assertThat(taskAReport.getAccountName()).isEqualTo("Account Alpha");
        assertThat(taskAReport.getTaskId()).isEqualTo(task1.getId());
        assertThat(taskAReport.getTotalHours()).isEqualTo(15.5);

        TaskReport taskBReport = aliceReport.getTaskReports().stream()
                .filter(tr -> "Task B".equals(tr.getTaskName()))
                .findFirst()
                .orElse(null);
        
        assertThat(taskBReport).isNotNull();
        assertThat(taskBReport.getTotalHours()).isEqualTo(6.0);
    }

    @Test
    void shouldGetUserReports() {
        List<UserReport> reports = reportRepository.getUserReports(fromDate, toDate);

        assertThat(reports).hasSize(3); // 2 users with time + 1 user without time

        // Check reports are ordered by total time desc for users with time
        UserReport firstReport = reports.stream()
                .filter(r -> r.getTotalTime() > 0)
                .findFirst()
                .orElse(null);
        
        assertThat(firstReport).isNotNull();
        assertThat(firstReport.getFullName()).isEqualTo("Alice Smith");
        assertThat(firstReport.getTotalTime()).isEqualTo(21.5);

        // Check user without time is included
        boolean charlieIncluded = reports.stream()
                .anyMatch(r -> "Charlie Brown".equals(r.getFullName()) && r.getTotalTime() == 0.0);
        assertThat(charlieIncluded).isTrue();
    }

    @Test
    void shouldGetTaskReports() {
        List<TaskReport> reports = reportRepository.getTaskReports(fromDate, toDate);

        assertThat(reports).hasSize(3);

        // Check Task A report (should have combined time from Alice and Bob)
        TaskReport taskAReport = reports.stream()
                .filter(tr -> "Task A".equals(tr.getTaskName()))
                .findFirst()
                .orElse(null);
        
        assertThat(taskAReport).isNotNull();
        assertThat(taskAReport.getAccountId()).isEqualTo(account1.getId());
        assertThat(taskAReport.getAccountName()).isEqualTo("Account Alpha");
        assertThat(taskAReport.getTaskId()).isEqualTo(task1.getId());
        assertThat(taskAReport.getTotalHours()).isEqualTo(24.0f); // Alice: 15.5 + Bob: 8.5

        // Check Task B report
        TaskReport taskBReport = reports.stream()
                .filter(tr -> "Task B".equals(tr.getTaskName()))
                .findFirst()
                .orElse(null);
        
        assertThat(taskBReport).isNotNull();
        assertThat(taskBReport.getTotalHours()).isEqualTo(6.0f);

        // Check Task C report
        TaskReport taskCReport = reports.stream()
                .filter(tr -> "Task C".equals(tr.getTaskName()))
                .findFirst()
                .orElse(null);
        
        assertThat(taskCReport).isNotNull();
        assertThat(taskCReport.getAccountId()).isEqualTo(account2.getId());
        assertThat(taskCReport.getAccountName()).isEqualTo("Account Beta");
        assertThat(taskCReport.getTotalHours()).isEqualTo(5.0f);
    }

    @Test
    void shouldGetAccountReports() {
        List<AccountReport> reports = reportRepository.getAccountReports(fromDate, toDate);

        assertThat(reports).hasSize(2);

        // Check Account Alpha report
        AccountReport account1Report = reports.stream()
                .filter(ar -> "Account Alpha".equals(ar.getAccountName()))
                .findFirst()
                .orElse(null);
        
        assertThat(account1Report).isNotNull();
        assertThat(account1Report.getAccountId()).isEqualTo(account1.getId());
        assertThat(account1Report.getTotalHours()).isEqualTo(30.0f); // Task A: 24.0 + Task B: 6.0

        // Check Account Beta report
        AccountReport account2Report = reports.stream()
                .filter(ar -> "Account Beta".equals(ar.getAccountName()))
                .findFirst()
                .orElse(null);
        
        assertThat(account2Report).isNotNull();
        assertThat(account2Report.getAccountId()).isEqualTo(account2.getId());
        assertThat(account2Report.getTotalHours()).isEqualTo(5.0f);
    }

    @Test
    void shouldReturnEmptyListsWhenNoDataInDateRange() {
        LocalDate futureFromDate = LocalDate.of(2025, 1, 1);
        LocalDate futureToDate = LocalDate.of(2025, 1, 31);

        List<UserReport> userTaskReports = reportRepository.getUserTaskReports(futureFromDate, futureToDate);
        List<UserReport> userReports = reportRepository.getUserReports(futureFromDate, futureToDate);
        List<TaskReport> taskReports = reportRepository.getTaskReports(futureFromDate, futureToDate);
        List<AccountReport> accountReports = reportRepository.getAccountReports(futureFromDate, futureToDate);

        // Should only include users with no reported time
        assertThat(userTaskReports).hasSize(3); // All users have no time in this range
        assertThat(userTaskReports).allMatch(ur -> ur.getTotalTime() == 0.0);
        
        assertThat(userReports).hasSize(3); // All users have no time in this range
        assertThat(userReports).allMatch(ur -> ur.getTotalTime() == 0.0);
        
        assertThat(taskReports).isEmpty();
        assertThat(accountReports).isEmpty();
    }

    @Test
    void shouldHandleDateRangeBoundaries() {
        // Test with exact date boundaries
        LocalDate exactFromDate = LocalDate.of(2024, 12, 15);
        LocalDate exactToDate = LocalDate.of(2024, 12, 18);

        List<TaskReport> reports = reportRepository.getTaskReports(exactFromDate, exactToDate);

        // Should include entries from 15th, 16th, 17th, and 18th
        assertThat(reports).hasSize(2); // Task A and Task B should have entries

        TaskReport taskAReport = reports.stream()
                .filter(tr -> "Task A".equals(tr.getTaskName()))
                .findFirst()
                .orElse(null);
        
        assertThat(taskAReport).isNotNull();
        assertThat(taskAReport.getTotalHours()).isEqualTo(24.0f); // Alice: 8.0 + 7.5 + Bob: 8.5
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

    private void createTimeEntry(TaskContributorPO contributor, LocalDate date, float time) {
        TimeEntryPO timeEntry = new TimeEntryPO();
        timeEntry.setTaskContributor(contributor);
        timeEntry.setDate(date);
        timeEntry.setTime(time);
        timeEntry.setCreatedBy(1L);
        timeEntry.setUpdatedBy(1L);
        timeEntry.setCreateDateTime(LocalDateTime.now());
        timeEntry.setUpdatedDateTime(LocalDateTime.now());
        timeEntryRepository.save(timeEntry);
    }
}