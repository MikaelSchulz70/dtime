package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TimeEntryRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TaskContributorRepository taskContributorRepository;

    @Test
    void shouldSaveAndFindTimeEntry() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        LocalDate entryDate = LocalDate.of(2024, 12, 31);
        BigDecimal reportedTime = BigDecimal.valueOf(7.5);

        TimeEntryPO timeEntry = createTimeEntry(contributor, entryDate, reportedTime);

        TimeEntryPO saved = timeEntryRepository.save(timeEntry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTaskContributor().getId()).isEqualTo(contributor.getId());
        assertThat(saved.getDate()).isEqualTo(entryDate);
        assertThat(saved.getTime()).isEqualTo(reportedTime);
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByTaskContributorAndDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        LocalDate entryDate = LocalDate.of(2024, 6, 30);
        TimeEntryPO timeEntry = createTimeEntry(contributor, entryDate, BigDecimal.valueOf(7.5));
        timeEntryRepository.save(timeEntry);

        TimeEntryPO found = timeEntryRepository.findByTaskContributorAndDate(contributor.getId(), entryDate);

        assertThat(found).isNotNull();
        assertThat(found.getTaskContributor().getId()).isEqualTo(contributor.getId());
        assertThat(found.getDate()).isEqualTo(entryDate);
        assertThat(found.getTime()).isEqualTo(BigDecimal.valueOf(7.5));
    }

    @Test
    void shouldReturnNullWhenTimeEntryNotFoundByTaskContributorAndDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        LocalDate nonExistentDate = LocalDate.of(2024, 1, 1);

        TimeEntryPO found = timeEntryRepository.findByTaskContributorAndDate(contributor.getId(), nonExistentDate);

        assertThat(found).isNull();
    }

    @Test
    void shouldFindByUserAndDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task1 = createAndSaveTask("Task 1", user);
        TaskPO task2 = createAndSaveTask("Task 2", user);
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user, task1);
        TaskContributorPO contributor2 = createAndSaveTaskContributor(user, task2);

        LocalDate entryDate = LocalDate.of(2024, 12, 31);
        TimeEntryPO entry1 = createTimeEntry(contributor1, entryDate, BigDecimal.valueOf(4.0));
        TimeEntryPO entry2 = createTimeEntry(contributor2, entryDate, BigDecimal.valueOf(3.5));

        timeEntryRepository.save(entry1);
        timeEntryRepository.save(entry2);

        List<TimeEntryPO> entries = timeEntryRepository.findByUserAndDate(user.getId(), entryDate);

        assertThat(entries).hasSize(2);
        assertThat(entries).extracting(TimeEntryPO::getTime)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(4.0), BigDecimal.valueOf(3.5));
    }

    @Test
    void shouldFindByUserAndBetweenDates() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        LocalDate beforeRange = LocalDate.of(2024, 11, 30);
        LocalDate afterRange = LocalDate.of(2025, 1, 1);

        TimeEntryPO entryInRange1 = createTimeEntry(contributor, LocalDate.of(2024, 12, 15), BigDecimal.valueOf(8.0));
        TimeEntryPO entryInRange2 = createTimeEntry(contributor, LocalDate.of(2024, 12, 20), BigDecimal.valueOf(7.5));
        TimeEntryPO entryBefore = createTimeEntry(contributor, beforeRange, BigDecimal.valueOf(6.0));
        TimeEntryPO entryAfter = createTimeEntry(contributor, afterRange, BigDecimal.valueOf(5.0));

        timeEntryRepository.save(entryInRange1);
        timeEntryRepository.save(entryInRange2);
        timeEntryRepository.save(entryBefore);
        timeEntryRepository.save(entryAfter);

        List<TimeEntryPO> entriesInRange = timeEntryRepository.findByUserAndBetweenDates(user.getId(), startDate, endDate);

        assertThat(entriesInRange).hasSize(2);
        assertThat(entriesInRange).extracting(TimeEntryPO::getTime)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(8.0), BigDecimal.valueOf(7.5));
    }

    @Test
    void shouldCountByUserId() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        TaskPO task = createAndSaveTask("Test Task", user1);
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user1, task);
        TaskContributorPO contributor2 = createAndSaveTaskContributor(user2, task);

        TimeEntryPO entry1 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 1), BigDecimal.valueOf(8.0));
        TimeEntryPO entry2 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 2), BigDecimal.valueOf(7.5));
        TimeEntryPO entry3 = createTimeEntry(contributor2, LocalDate.of(2024, 12, 3), BigDecimal.valueOf(6.0));

        timeEntryRepository.save(entry1);
        timeEntryRepository.save(entry2);
        timeEntryRepository.save(entry3);

        long user1Count = timeEntryRepository.countByUserId(user1.getId());
        long user2Count = timeEntryRepository.countByUserId(user2.getId());

        assertThat(user1Count).isEqualTo(2);
        assertThat(user2Count).isEqualTo(1);
    }

    @Test
    void shouldCountByTask() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task1 = createAndSaveTask("Task 1", user);
        TaskPO task2 = createAndSaveTask("Task 2", user);
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user, task1);
        TaskContributorPO contributor2 = createAndSaveTaskContributor(user, task2);

        TimeEntryPO entry1 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 1), BigDecimal.valueOf(8.0));
        TimeEntryPO entry2 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 2), BigDecimal.valueOf(7.5));
        TimeEntryPO entry3 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 3), BigDecimal.valueOf(6.0));
        TimeEntryPO entry4 = createTimeEntry(contributor2, LocalDate.of(2024, 12, 4), BigDecimal.valueOf(5.0));

        timeEntryRepository.save(entry1);
        timeEntryRepository.save(entry2);
        timeEntryRepository.save(entry3);
        timeEntryRepository.save(entry4);

        long task1Count = timeEntryRepository.countByTask(task1.getId());
        long task2Count = timeEntryRepository.countByTask(task2.getId());

        assertThat(task1Count).isEqualTo(3);
        assertThat(task2Count).isEqualTo(1);
    }

    @Test
    void shouldFindByTaskAndBetweenDates() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        TimeEntryPO entryInRange1 = createTimeEntry(contributor, LocalDate.of(2024, 12, 10), BigDecimal.valueOf(8.0));
        TimeEntryPO entryInRange2 = createTimeEntry(contributor, LocalDate.of(2024, 12, 20), BigDecimal.valueOf(7.5));
        TimeEntryPO entryOutOfRange = createTimeEntry(contributor, LocalDate.of(2025, 1, 5), BigDecimal.valueOf(6.0));

        timeEntryRepository.save(entryInRange1);
        timeEntryRepository.save(entryInRange2);
        timeEntryRepository.save(entryOutOfRange);

        List<TimeEntryPO> entriesInRange = timeEntryRepository.findByTaskAndBetweenDates(task.getId(), startDate, endDate);

        assertThat(entriesInRange).hasSize(2);
        assertThat(entriesInRange).extracting(TimeEntryPO::getTime)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(8.0), BigDecimal.valueOf(7.5));
    }

    @Test
    void shouldDeleteById() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        TimeEntryPO timeEntry = createTimeEntry(contributor, LocalDate.of(2024, 12, 31), BigDecimal.valueOf(8.0));
        TimeEntryPO saved = timeEntryRepository.save(timeEntry);

        timeEntryRepository.deleteById(saved.getId());

        Optional<TimeEntryPO> found = timeEntryRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindByUser() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        TaskPO task = createAndSaveTask("Test Task", user1);
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user1, task);
        TaskContributorPO contributor2 = createAndSaveTaskContributor(user2, task);

        TimeEntryPO entry1 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 1), BigDecimal.valueOf(8.0));
        TimeEntryPO entry2 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 2), BigDecimal.valueOf(7.5));
        TimeEntryPO entry3 = createTimeEntry(contributor2, LocalDate.of(2024, 12, 3), BigDecimal.valueOf(6.0));

        timeEntryRepository.save(entry1);
        timeEntryRepository.save(entry2);
        timeEntryRepository.save(entry3);

        List<TimeEntryPO> user1Entries = timeEntryRepository.findByUser(user1.getId());
        List<TimeEntryPO> user2Entries = timeEntryRepository.findByUser(user2.getId());

        assertThat(user1Entries).hasSize(2);
        assertThat(user1Entries).extracting(TimeEntryPO::getTime)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(8.0), BigDecimal.valueOf(7.5));

        assertThat(user2Entries).hasSize(1);
        assertThat(user2Entries.get(0).getTime()).isEqualTo(BigDecimal.valueOf(6.0));
    }

    @Test
    void shouldFindByTask() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task1 = createAndSaveTask("Task 1", user);
        TaskPO task2 = createAndSaveTask("Task 2", user);
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user, task1);
        TaskContributorPO contributor2 = createAndSaveTaskContributor(user, task2);

        TimeEntryPO entry1 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 1), BigDecimal.valueOf(8.0));
        TimeEntryPO entry2 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 2), BigDecimal.valueOf(7.5));
        TimeEntryPO entry3 = createTimeEntry(contributor2, LocalDate.of(2024, 12, 3), BigDecimal.valueOf(6.0));

        timeEntryRepository.save(entry1);
        timeEntryRepository.save(entry2);
        timeEntryRepository.save(entry3);

        List<TimeEntryPO> task1Entries = timeEntryRepository.findByTask(task1.getId());
        List<TimeEntryPO> task2Entries = timeEntryRepository.findByTask(task2.getId());

        assertThat(task1Entries).hasSize(2);
        assertThat(task1Entries).extracting(TimeEntryPO::getTime)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(8.0), BigDecimal.valueOf(7.5));

        assertThat(task2Entries).hasSize(1);
        assertThat(task2Entries.get(0).getTime()).isEqualTo(BigDecimal.valueOf(6.0));
    }

    @Test
    void shouldFindByAccount() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        AccountPO account1 = createAndSaveAccount("Account 1");
        AccountPO account2 = createAndSaveAccount("Account 2");
        TaskPO task1 = createAndSaveTaskWithAccount("Task 1", account1);
        TaskPO task2 = createAndSaveTaskWithAccount("Task 2", account2);
        TaskContributorPO contributor1 = createAndSaveTaskContributor(user, task1);
        TaskContributorPO contributor2 = createAndSaveTaskContributor(user, task2);

        TimeEntryPO entry1 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 1), BigDecimal.valueOf(8.0));
        TimeEntryPO entry2 = createTimeEntry(contributor1, LocalDate.of(2024, 12, 2), BigDecimal.valueOf(7.5));
        TimeEntryPO entry3 = createTimeEntry(contributor2, LocalDate.of(2024, 12, 3), BigDecimal.valueOf(6.0));

        timeEntryRepository.save(entry1);
        timeEntryRepository.save(entry2);
        timeEntryRepository.save(entry3);

        List<TimeEntryPO> account1Entries = timeEntryRepository.findByAccount(account1.getId());
        List<TimeEntryPO> account2Entries = timeEntryRepository.findByAccount(account2.getId());

        assertThat(account1Entries).hasSize(2);
        assertThat(account1Entries).extracting(TimeEntryPO::getTime)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(8.0), BigDecimal.valueOf(7.5));

        assertThat(account2Entries).hasSize(1);
        assertThat(account2Entries.get(0).getTime()).isEqualTo(BigDecimal.valueOf(6.0));
    }

    @Test
    void shouldUpdateTimeEntry() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        TimeEntryPO timeEntry = createTimeEntry(contributor, LocalDate.of(2024, 12, 31), BigDecimal.valueOf(8.0));
        TimeEntryPO saved = timeEntryRepository.save(timeEntry);

        saved.setTime(BigDecimal.valueOf(9.5));
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());

        TimeEntryPO updated = timeEntryRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getTime()).isEqualTo(BigDecimal.valueOf(9.5));
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2024, 12, 31)); // Date should remain unchanged (updatable = false)
    }

    @Test
    void shouldHandleFloatPrecision() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);
        TaskContributorPO contributor = createAndSaveTaskContributor(user, task);

        BigDecimal preciseTime = BigDecimal.valueOf(7.25);
        TimeEntryPO timeEntry = createTimeEntry(contributor, LocalDate.of(2024, 12, 31), preciseTime);
        TimeEntryPO saved = timeEntryRepository.save(timeEntry);

        assertThat(saved.getTime()).isEqualTo(preciseTime);
    }

    private TimeEntryPO createTimeEntry(TaskContributorPO contributor, LocalDate date, BigDecimal time) {
        TimeEntryPO timeEntry = new TimeEntryPO();
        timeEntry.setTaskContributor(contributor);
        timeEntry.setDate(date);
        timeEntry.setTime(time);
        timeEntry.setCreatedBy(1L);
        timeEntry.setUpdatedBy(1L);
        timeEntry.setCreateDateTime(LocalDateTime.now());
        timeEntry.setUpdatedDateTime(LocalDateTime.now());
        return timeEntry;
    }

    private UserPO createAndSaveUser(String email, String firstName, String lastName) {
        UserPO user = new UserPO();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setExternalId("ext-" + email);
        user.setUserRole(UserRole.USER);
        user.setActivationStatus(ActivationStatus.ACTIVE);
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        user.setCreateDateTime(LocalDateTime.now());
        user.setUpdatedDateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    private TaskPO createAndSaveTask(String name, UserPO user) {
        AccountPO account = createAndSaveAccount("Account for " + name);
        return createAndSaveTaskWithAccount(name, account);
    }

    private TaskPO createAndSaveTaskWithAccount(String name, AccountPO account) {
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
}