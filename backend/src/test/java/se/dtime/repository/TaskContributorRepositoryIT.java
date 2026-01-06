package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskContributorRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private TaskContributorRepository taskContributorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindTaskContributor() {
        UserPO user = createAndSaveUser("john@example.com", "John", "Doe");
        TaskPO task = createAndSaveTask("Test Task", user);

        TaskContributorPO contributor = createTaskContributor(user, task, ActivationStatus.ACTIVE);

        TaskContributorPO saved = taskContributorRepository.save(contributor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getTask().getId()).isEqualTo(task.getId());
        assertThat(saved.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByUser() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        TaskPO task1 = createAndSaveTask("Task 1", user1);
        TaskPO task2 = createAndSaveTask("Task 2", user1);
        TaskPO task3 = createAndSaveTask("Task 3", user2);

        TaskContributorPO contributor1 = createTaskContributor(user1, task1, ActivationStatus.ACTIVE);
        TaskContributorPO contributor2 = createTaskContributor(user1, task2, ActivationStatus.ACTIVE);
        TaskContributorPO contributor3 = createTaskContributor(user2, task3, ActivationStatus.ACTIVE);

        taskContributorRepository.save(contributor1);
        taskContributorRepository.save(contributor2);
        taskContributorRepository.save(contributor3);

        List<TaskContributorPO> user1Contributors = taskContributorRepository.findByUser(user1);

        assertThat(user1Contributors).hasSize(2);
        assertThat(user1Contributors).allMatch(tc -> tc.getUser().getId().equals(user1.getId()));

        List<TaskContributorPO> user2Contributors = taskContributorRepository.findByUser(user2);

        assertThat(user2Contributors).hasSize(1);
        assertThat(user2Contributors.get(0).getUser().getId()).isEqualTo(user2.getId());
    }

    @Test
    void shouldFindByTask() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        TaskPO task1 = createAndSaveTask("Task 1", user1);
        TaskPO task2 = createAndSaveTask("Task 2", user1);

        TaskContributorPO contributor1 = createTaskContributor(user1, task1, ActivationStatus.ACTIVE);
        TaskContributorPO contributor2 = createTaskContributor(user2, task1, ActivationStatus.ACTIVE);
        TaskContributorPO contributor3 = createTaskContributor(user1, task2, ActivationStatus.ACTIVE);

        taskContributorRepository.save(contributor1);
        taskContributorRepository.save(contributor2);
        taskContributorRepository.save(contributor3);

        List<TaskContributorPO> task1Contributors = taskContributorRepository.findByTask(task1);

        assertThat(task1Contributors).hasSize(2);
        assertThat(task1Contributors).allMatch(tc -> tc.getTask().getId().equals(task1.getId()));

        List<TaskContributorPO> task2Contributors = taskContributorRepository.findByTask(task2);

        assertThat(task2Contributors).hasSize(1);
        assertThat(task2Contributors.get(0).getTask().getId()).isEqualTo(task2.getId());
    }

    @Test
    void shouldFindByUserAndActivationStatus() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task1 = createAndSaveTask("Task 1", user);
        TaskPO task2 = createAndSaveTask("Task 2", user);
        TaskPO task3 = createAndSaveTask("Task 3", user);

        TaskContributorPO activeContributor1 = createTaskContributor(user, task1, ActivationStatus.ACTIVE);
        TaskContributorPO activeContributor2 = createTaskContributor(user, task2, ActivationStatus.ACTIVE);
        TaskContributorPO inactiveContributor = createTaskContributor(user, task3, ActivationStatus.INACTIVE);

        taskContributorRepository.save(activeContributor1);
        taskContributorRepository.save(activeContributor2);
        taskContributorRepository.save(inactiveContributor);

        List<TaskContributorPO> activeContributors = taskContributorRepository.findByUserAndActivationStatus(user, ActivationStatus.ACTIVE);

        assertThat(activeContributors).hasSize(2);
        assertThat(activeContributors).allMatch(tc -> tc.getActivationStatus() == ActivationStatus.ACTIVE);
        assertThat(activeContributors).allMatch(tc -> tc.getUser().getId().equals(user.getId()));

        List<TaskContributorPO> inactiveContributors = taskContributorRepository.findByUserAndActivationStatus(user, ActivationStatus.INACTIVE);

        assertThat(inactiveContributors).hasSize(1);
        assertThat(inactiveContributors.get(0).getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
    }

    @Test
    void shouldFindByUserAndTask() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        TaskPO task1 = createAndSaveTask("Task 1", user1);
        TaskPO task2 = createAndSaveTask("Task 2", user1);

        TaskContributorPO contributor1 = createTaskContributor(user1, task1, ActivationStatus.ACTIVE);
        TaskContributorPO contributor2 = createTaskContributor(user2, task1, ActivationStatus.ACTIVE);
        TaskContributorPO contributor3 = createTaskContributor(user1, task2, ActivationStatus.ACTIVE);

        taskContributorRepository.save(contributor1);
        taskContributorRepository.save(contributor2);
        taskContributorRepository.save(contributor3);

        TaskContributorPO found = taskContributorRepository.findByUserAndTask(user1, task1);

        assertThat(found).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(user1.getId());
        assertThat(found.getTask().getId()).isEqualTo(task1.getId());

        TaskContributorPO notFound = taskContributorRepository.findByUserAndTask(user2, task2);

        assertThat(notFound).isNull();
    }

    @Test
    void shouldCountByUser() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        TaskPO task1 = createAndSaveTask("Task 1", user1);
        TaskPO task2 = createAndSaveTask("Task 2", user1);
        TaskPO task3 = createAndSaveTask("Task 3", user2);

        TaskContributorPO contributor1 = createTaskContributor(user1, task1, ActivationStatus.ACTIVE);
        TaskContributorPO contributor2 = createTaskContributor(user1, task2, ActivationStatus.ACTIVE);
        TaskContributorPO contributor3 = createTaskContributor(user2, task3, ActivationStatus.ACTIVE);

        taskContributorRepository.save(contributor1);
        taskContributorRepository.save(contributor2);
        taskContributorRepository.save(contributor3);

        long user1Count = taskContributorRepository.countByUser(user1);
        long user2Count = taskContributorRepository.countByUser(user2);

        assertThat(user1Count).isEqualTo(2);
        assertThat(user2Count).isEqualTo(1);
    }

    @Test
    void shouldReturnZeroWhenCountingUserWithNoContributions() {
        UserPO user = createAndSaveUser("empty@example.com", "Empty", "User");

        long count = taskContributorRepository.countByUser(user);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldUpdateTaskContributor() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);

        TaskContributorPO contributor = createTaskContributor(user, task, ActivationStatus.ACTIVE);
        TaskContributorPO saved = taskContributorRepository.save(contributor);

        saved.setActivationStatus(ActivationStatus.INACTIVE);
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());

        TaskContributorPO updated = taskContributorRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldDeleteTaskContributor() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        TaskPO task = createAndSaveTask("Test Task", user);

        TaskContributorPO contributor = createTaskContributor(user, task, ActivationStatus.ACTIVE);
        TaskContributorPO saved = taskContributorRepository.save(contributor);

        taskContributorRepository.delete(saved);

        TaskContributorPO found = taskContributorRepository.findByUserAndTask(user, task);
        assertThat(found).isNull();
    }

    private TaskContributorPO createTaskContributor(UserPO user, TaskPO task, ActivationStatus status) {
        TaskContributorPO contributor = new TaskContributorPO();
        contributor.setUser(user);
        contributor.setTask(task);
        contributor.setActivationStatus(status);
        contributor.setCreatedBy(1L);
        contributor.setUpdatedBy(1L);
        contributor.setCreateDateTime(LocalDateTime.now());
        contributor.setUpdatedDateTime(LocalDateTime.now());
        return contributor;
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

    private TaskPO createAndSaveTask(String name, UserPO user) {
        AccountPO account = new AccountPO();
        account.setName("Account for " + name);
        account.setActivationStatus(ActivationStatus.ACTIVE);
        account.setCreatedBy(1L);
        account.setUpdatedBy(1L);
        account.setCreateDateTime(LocalDateTime.now());
        account.setUpdatedDateTime(LocalDateTime.now());
        AccountPO savedAccount = accountRepository.save(account);

        TaskPO task = new TaskPO();
        task.setName(name);
        task.setActivationStatus(ActivationStatus.ACTIVE);
        task.setAccount(savedAccount);
        task.setIsBillable(false);
        task.setCreatedBy(1L);
        task.setUpdatedBy(1L);
        task.setCreateDateTime(LocalDateTime.now());
        task.setUpdatedDateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }
}