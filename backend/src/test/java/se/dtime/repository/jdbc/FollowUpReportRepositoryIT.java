package se.dtime.repository.jdbc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;
import se.dtime.model.report.FollowUpData;
import se.dtime.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true;INIT=CREATE SCHEMA IF NOT EXISTS \"public\"",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC"
})
@Import(FollowUpReportRepository.class)
@Transactional
class FollowUpReportRepositoryIT {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FollowUpReportRepository followUpReportRepository;

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

    private UserPO user1, user2;
    private AccountPO account1, account2;
    private TaskPO task1, task2;
    private TaskContributorPO contributor1, contributor2;
    private LocalDate fromDate, toDate;

    @BeforeEach
    void setUp() {
        // Create the rate table that the query references (even though it's not used in our test)
        createRateTable();

        // Create test data
        fromDate = LocalDate.of(2024, 12, 1);
        toDate = LocalDate.of(2024, 12, 31);

        // Create users
        user1 = createAndSaveUser("alice@example.com", "Alice", "Smith");
        user2 = createAndSaveUser("bob@example.com", "Bob", "Johnson");

        // Create accounts
        account1 = createAndSaveAccount("Account Alpha");
        account2 = createAndSaveAccount("Account Beta");

        // Create tasks
        task1 = createAndSaveTask("Task A", account1);
        task2 = createAndSaveTask("Task B", account2);

        // Create task contributors
        contributor1 = createAndSaveTaskContributor(user1, task1);
        contributor2 = createAndSaveTaskContributor(user2, task2);

        // Create time entries
        createTimeEntry(contributor1, LocalDate.of(2024, 12, 15), 8.0f);
        createTimeEntry(contributor1, LocalDate.of(2024, 12, 16), 7.5f);
        createTimeEntry(contributor2, LocalDate.of(2024, 12, 17), 6.0f);
        createTimeEntry(contributor2, LocalDate.of(2024, 12, 18), 4.5f);

        // Time entry outside date range
        createTimeEntry(contributor1, LocalDate.of(2024, 11, 30), 3.0f);

        // Flush and clear to ensure data is committed to database for JDBC queries
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldGetFollowUpData() {
        List<FollowUpData> followUpDataList = followUpReportRepository.getFollowUpData(fromDate, toDate);

        assertThat(followUpDataList).hasSize(2);

        // Check Alice's follow-up data
        FollowUpData aliceData = followUpDataList.stream()
                .filter(fud -> "Alice Smith".equals(fud.getFullName()))
                .findFirst()
                .orElse(null);

        assertThat(aliceData).isNotNull();
        assertThat(aliceData.getAccountId()).isEqualTo(account1.getId());
        assertThat(aliceData.getAccountName()).isEqualTo("Account Alpha");
        assertThat(aliceData.getTaskId()).isEqualTo(task1.getId());
        assertThat(aliceData.getTaskName()).isEqualTo("Task A");
        assertThat(aliceData.getuserId()).isEqualTo(user1.getId());
        assertThat(aliceData.getTotalTime()).isEqualTo(new BigDecimal("15.5")); // 8.0 + 7.5

        // Check Bob's follow-up data
        FollowUpData bobData = followUpDataList.stream()
                .filter(fud -> "Bob Johnson".equals(fud.getFullName()))
                .findFirst()
                .orElse(null);

        assertThat(bobData).isNotNull();
        assertThat(bobData.getAccountId()).isEqualTo(account2.getId());
        assertThat(bobData.getAccountName()).isEqualTo("Account Beta");
        assertThat(bobData.getTaskId()).isEqualTo(task2.getId());
        assertThat(bobData.getTaskName()).isEqualTo("Task B");
        assertThat(bobData.getuserId()).isEqualTo(user2.getId());
        assertThat(bobData.getTotalTime()).isEqualTo(new BigDecimal("10.5")); // 6.0 + 4.5
    }

    @Test
    void shouldReturnEmptyListWhenNoDataInDateRange() {
        LocalDate futureFromDate = LocalDate.of(2025, 1, 1);
        LocalDate futureToDate = LocalDate.of(2025, 1, 31);

        List<FollowUpData> followUpDataList = followUpReportRepository.getFollowUpData(futureFromDate, futureToDate);

        assertThat(followUpDataList).isEmpty();
    }

    @Test
    void shouldHandleDateRangeBoundaries() {
        // Test with exact date boundaries
        LocalDate exactFromDate = LocalDate.of(2024, 12, 16);
        LocalDate exactToDate = LocalDate.of(2024, 12, 17);

        List<FollowUpData> followUpDataList = followUpReportRepository.getFollowUpData(exactFromDate, exactToDate);

        assertThat(followUpDataList).hasSize(2);

        // Check Alice's data (should only include 16th)
        FollowUpData aliceData = followUpDataList.stream()
                .filter(fud -> "Alice Smith".equals(fud.getFullName()))
                .findFirst()
                .orElse(null);

        assertThat(aliceData).isNotNull();
        assertThat(aliceData.getTotalTime()).isEqualTo(new BigDecimal("7.5"));

        // Check Bob's data (should only include 17th)
        FollowUpData bobData = followUpDataList.stream()
                .filter(fud -> "Bob Johnson".equals(fud.getFullName()))
                .findFirst()
                .orElse(null);

        assertThat(bobData).isNotNull();
        assertThat(bobData.getTotalTime()).isEqualTo(new BigDecimal("6.0"));
    }

    @Test
    void shouldOrderByAccountNameAndTaskNameDesc() {
        // Add more test data to verify ordering
        AccountPO account3 = createAndSaveAccount("Account Charlie");
        TaskPO task3 = createAndSaveTask("Task Z", account3);
        TaskContributorPO contributor3 = createAndSaveTaskContributor(user1, task3);
        createTimeEntry(contributor3, LocalDate.of(2024, 12, 20), 2.0f);

        List<FollowUpData> followUpDataList = followUpReportRepository.getFollowUpData(fromDate, toDate);

        assertThat(followUpDataList).hasSize(2);

        // Should be ordered by account name, then task name desc
        // Account Alpha - Task A, Account Beta - Task B, Account Charlie - Task Z
        assertThat(followUpDataList.get(0).getAccountName()).isEqualTo("Account Alpha");
        assertThat(followUpDataList.get(1).getAccountName()).isEqualTo("Account Beta");
    }

    @Test
    void shouldHandleMultipleTaskContributorsForSameTask() {
        // Add second contributor to same task
        TaskContributorPO contributor3 = createAndSaveTaskContributor(user2, task1);
        createTimeEntry(contributor3, LocalDate.of(2024, 12, 19), 5.0f);

        List<FollowUpData> followUpDataList = followUpReportRepository.getFollowUpData(fromDate, toDate);

        assertThat(followUpDataList).hasSize(2);

        // Should have separate entries for each user-task combination
        long task1Entries = followUpDataList.stream()
                .filter(fud -> fud.getTaskId() == task1.getId())
                .count();

        assertThat(task1Entries).isEqualTo(1L); // Alice and Bob both worked on Task A
    }

    @Test
    void shouldExcludeEntriesWithZeroTime() {
        // The query uses "having sum(reportedtime) > 0" so entries with 0 time should be excluded
        // Create a task contributor but don't add any time entries
        UserPO user3 = createAndSaveUser("charlie@example.com", "Charlie", "Brown");
        TaskPO task3 = createAndSaveTask("Task C", account1);
        createAndSaveTaskContributor(user3, task3);
        // No time entries for this contributor

        List<FollowUpData> followUpDataList = followUpReportRepository.getFollowUpData(fromDate, toDate);

        // Should not include Charlie since he has no time entries
        boolean charlieIncluded = followUpDataList.stream()
                .anyMatch(fud -> "Charlie Brown".equals(fud.getFullName()));

        assertThat(charlieIncluded).isFalse();
    }

    private void createRateTable() {
        // Create a minimal rate table for the left join to work
        // This table is referenced in the query but not used in our current test data
        jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS rate (
                        id BIGINT PRIMARY KEY,
                        id_task_contributor BIGINT,
                        fromdate DATE,
                        todate DATE,
                        comment VARCHAR(255)
                    )
                """);
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