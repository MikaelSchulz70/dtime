package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.ActivationStatus;

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
class TaskRepositoryIT {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindTask() {
        AccountPO account = createAccount("Test Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        TaskPO task = createTask("Test Task", ActivationStatus.ACTIVE, savedAccount);
        
        TaskPO saved = taskRepository.save(task);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Task");
        assertThat(saved.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(saved.getAccount().getId()).isEqualTo(savedAccount.getId());
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByName() {
        AccountPO account = createAccount("Test Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        TaskPO task = createTask("Find By Name Task", ActivationStatus.ACTIVE, savedAccount);
        taskRepository.save(task);
        
        List<TaskPO> found = taskRepository.findByName("Find By Name Task");
        
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Find By Name Task");
        assertThat(found.get(0).getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
    }

    @Test
    void shouldReturnEmptyListWhenTaskNotFoundByName() {
        List<TaskPO> found = taskRepository.findByName("Nonexistent Task");
        
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindByActivationStatus() {
        AccountPO account = createAccount("Test Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        TaskPO activeTask1 = createTask("Active Task 1", ActivationStatus.ACTIVE, savedAccount);
        TaskPO activeTask2 = createTask("Active Task 2", ActivationStatus.ACTIVE, savedAccount);
        TaskPO inactiveTask = createTask("Inactive Task", ActivationStatus.INACTIVE, savedAccount);
        
        taskRepository.save(activeTask1);
        taskRepository.save(activeTask2);
        taskRepository.save(inactiveTask);
        
        List<TaskPO> activeTasks = taskRepository.findByActivationStatus(ActivationStatus.ACTIVE);
        
        assertThat(activeTasks).hasSize(2);
        assertThat(activeTasks).allMatch(task -> task.getActivationStatus() == ActivationStatus.ACTIVE);
        assertThat(activeTasks).extracting(TaskPO::getName)
            .containsExactlyInAnyOrder("Active Task 1", "Active Task 2");
    }

    @Test
    void shouldFindInactiveTasksByStatus() {
        AccountPO account = createAccount("Test Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        TaskPO activeTask = createTask("Active Task", ActivationStatus.ACTIVE, savedAccount);
        TaskPO inactiveTask = createTask("Inactive Task", ActivationStatus.INACTIVE, savedAccount);
        
        taskRepository.save(activeTask);
        taskRepository.save(inactiveTask);
        
        List<TaskPO> inactiveTasks = taskRepository.findByActivationStatus(ActivationStatus.INACTIVE);
        
        assertThat(inactiveTasks).hasSize(1);
        assertThat(inactiveTasks.get(0).getName()).isEqualTo("Inactive Task");
        assertThat(inactiveTasks.get(0).getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
    }

    @Test
    void shouldFindByAccount() {
        AccountPO account1 = createAccount("Account 1");
        AccountPO account2 = createAccount("Account 2");
        AccountPO savedAccount1 = accountRepository.save(account1);
        AccountPO savedAccount2 = accountRepository.save(account2);
        
        TaskPO task1 = createTask("Task 1", ActivationStatus.ACTIVE, savedAccount1);
        TaskPO task2 = createTask("Task 2", ActivationStatus.ACTIVE, savedAccount1);
        TaskPO task3 = createTask("Task 3", ActivationStatus.ACTIVE, savedAccount2);
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        List<TaskPO> account1Tasks = taskRepository.findByAccount(savedAccount1);
        
        assertThat(account1Tasks).hasSize(2);
        assertThat(account1Tasks).extracting(TaskPO::getName)
            .containsExactlyInAnyOrder("Task 1", "Task 2");
        assertThat(account1Tasks).allMatch(task -> task.getAccount().getId().equals(savedAccount1.getId()));
        
        List<TaskPO> account2Tasks = taskRepository.findByAccount(savedAccount2);
        
        assertThat(account2Tasks).hasSize(1);
        assertThat(account2Tasks.get(0).getName()).isEqualTo("Task 3");
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksFoundByAccount() {
        AccountPO account = createAccount("Empty Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        List<TaskPO> tasks = taskRepository.findByAccount(savedAccount);
        
        assertThat(tasks).isEmpty();
    }

    @Test
    void shouldUpdateTask() {
        AccountPO account = createAccount("Test Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        TaskPO task = createTask("Update Task", ActivationStatus.ACTIVE, savedAccount);
        TaskPO saved = taskRepository.save(task);
        
        saved.setActivationStatus(ActivationStatus.INACTIVE);
        saved.setName("Updated Task Name");
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());
        
        TaskPO updated = taskRepository.save(saved);
        
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        assertThat(updated.getName()).isEqualTo("Updated Task Name");
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldDeleteTask() {
        AccountPO account = createAccount("Test Account");
        AccountPO savedAccount = accountRepository.save(account);
        
        TaskPO task = createTask("Delete Task", ActivationStatus.ACTIVE, savedAccount);
        TaskPO saved = taskRepository.save(task);
        
        taskRepository.delete(saved);
        
        List<TaskPO> found = taskRepository.findByName("Delete Task");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        TaskPO task1 = new TaskPO(1L);
        TaskPO task2 = new TaskPO(1L);
        TaskPO task3 = new TaskPO(2L);
        
        assertThat(task1).isEqualTo(task2);
        assertThat(task1).isNotEqualTo(task3);
        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
        assertThat(task1.hashCode()).isNotEqualTo(task3.hashCode());
    }

    private TaskPO createTask(String name, ActivationStatus status, AccountPO account) {
        TaskPO task = new TaskPO();
        task.setName(name);
        task.setActivationStatus(status);
        task.setAccount(account);
        task.setCreatedBy(1L);
        task.setUpdatedBy(1L);
        task.setCreateDateTime(LocalDateTime.now());
        task.setUpdatedDateTime(LocalDateTime.now());
        return task;
    }

    private AccountPO createAccount(String name) {
        AccountPO account = new AccountPO();
        account.setName(name);
        account.setActivationStatus(ActivationStatus.ACTIVE);
        account.setCreatedBy(1L);
        account.setUpdatedBy(1L);
        account.setCreateDateTime(LocalDateTime.now());
        account.setUpdatedDateTime(LocalDateTime.now());
        return account;
    }
}