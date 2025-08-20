package se.dtime.restcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.SystemPropertyType;
import se.dtime.model.UserRole;
import se.dtime.repository.AccountRepository;
import se.dtime.repository.SystemPropertyRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true;INIT=CREATE SCHEMA IF NOT EXISTS \"public\"",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
    "spring.jpa.properties.hibernate.default_schema=PUBLIC",
    "security.enable-csrf=false"
})
@Transactional
public abstract class BaseRestControllerIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected SystemPropertyRepository systemPropertyRepository;

    protected UserPO testUser;
    protected UserPO testAdmin;
    protected AccountPO testAccount;
    protected TaskPO testTask;
    protected SystemPropertyPO testSystemProperty;

    @BeforeEach
    void setUpBaseData() {
        // Create test users
        testUser = createAndSaveUser("user@example.com", "Test", "User", UserRole.USER);
        testAdmin = createAndSaveUser("admin@example.com", "Test", "Admin", UserRole.ADMIN);
        
        // Create test account
        testAccount = createAndSaveAccount("Test Account");
        
        // Create test task
        testTask = createAndSaveTask("Test Task", testAccount);
        
        // Create test system property
        testSystemProperty = createAndSaveSystemProperty("test.property", "test value");
    }

    protected UserPO createAndSaveUser(String email, String firstName, String lastName, UserRole role) {
        UserPO user = new UserPO();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("$2a$10$DzN7xtUaWr1H7HlNYT2yMOMPHpTIgvzLFIJaFMWrdEUwrHfRKrEYW"); // BCrypt for "oldpassword"
        user.setUserRole(role);
        user.setActivationStatus(ActivationStatus.ACTIVE);
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        user.setCreateDateTime(LocalDateTime.now());
        user.setUpdatedDateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    protected AccountPO createAndSaveAccount(String name) {
        AccountPO account = new AccountPO();
        account.setName(name);
        account.setActivationStatus(ActivationStatus.ACTIVE);
        account.setCreatedBy(1L);
        account.setUpdatedBy(1L);
        account.setCreateDateTime(LocalDateTime.now());
        account.setUpdatedDateTime(LocalDateTime.now());
        return accountRepository.save(account);
    }

    protected TaskPO createAndSaveTask(String name, AccountPO account) {
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

    protected SystemPropertyPO createAndSaveSystemProperty(String name, String value) {
        SystemPropertyPO systemProperty = new SystemPropertyPO();
        systemProperty.setName(name);
        systemProperty.setValue(value);
        systemProperty.setSystemPropertyType(SystemPropertyType.TEXT);
        systemProperty.setDescription("Test system property");
        systemProperty.setCreatedBy(1L);
        systemProperty.setUpdatedBy(1L);
        systemProperty.setCreateDateTime(LocalDateTime.now());
        systemProperty.setUpdatedDateTime(LocalDateTime.now());
        return systemPropertyRepository.save(systemProperty);
    }

    protected String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}