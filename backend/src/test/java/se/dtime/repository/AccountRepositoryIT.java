package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.AccountPO;
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
class AccountRepositoryIT {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindAccount() {
        AccountPO account = createAccount("testuser", ActivationStatus.ACTIVE);
        
        AccountPO saved = accountRepository.save(account);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("testuser");
        assertThat(saved.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByName() {
        AccountPO account = createAccount("findbyname", ActivationStatus.ACTIVE);
        accountRepository.save(account);
        
        AccountPO found = accountRepository.findByName("findbyname");
        
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("findbyname");
        assertThat(found.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
    }

    @Test
    void shouldReturnNullWhenAccountNotFoundByName() {
        AccountPO found = accountRepository.findByName("nonexistent");
        
        assertThat(found).isNull();
    }

    @Test
    void shouldFindByActivationStatusOrderByNameAsc() {
        AccountPO activeAccount1 = createAccount("charlie", ActivationStatus.ACTIVE);
        AccountPO activeAccount2 = createAccount("alice", ActivationStatus.ACTIVE);
        AccountPO inactiveAccount = createAccount("bob", ActivationStatus.INACTIVE);
        
        accountRepository.save(activeAccount1);
        accountRepository.save(activeAccount2);
        accountRepository.save(inactiveAccount);
        
        List<AccountPO> activeAccounts = accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.ACTIVE);
        
        assertThat(activeAccounts).hasSize(2);
        assertThat(activeAccounts.get(0).getName()).isEqualTo("alice");
        assertThat(activeAccounts.get(1).getName()).isEqualTo("charlie");
        assertThat(activeAccounts).allMatch(account -> account.getActivationStatus() == ActivationStatus.ACTIVE);
    }

    @Test
    void shouldFindInactiveAccountsByStatus() {
        AccountPO activeAccount = createAccount("active", ActivationStatus.ACTIVE);
        AccountPO inactiveAccount = createAccount("inactive", ActivationStatus.INACTIVE);
        
        accountRepository.save(activeAccount);
        accountRepository.save(inactiveAccount);
        
        List<AccountPO> inactiveAccounts = accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.INACTIVE);
        
        assertThat(inactiveAccounts).hasSize(1);
        assertThat(inactiveAccounts.get(0).getName()).isEqualTo("inactive");
        assertThat(inactiveAccounts.get(0).getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsFoundByStatus() {
        List<AccountPO> accounts = accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.ACTIVE);
        
        assertThat(accounts).isEmpty();
    }

    @Test
    void shouldUpdateAccount() {
        AccountPO account = createAccount("updatetest", ActivationStatus.ACTIVE);
        AccountPO saved = accountRepository.save(account);
        
        saved.setActivationStatus(ActivationStatus.INACTIVE);
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());
        
        AccountPO updated = accountRepository.save(saved);
        
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldDeleteAccount() {
        AccountPO account = createAccount("deletetest", ActivationStatus.ACTIVE);
        AccountPO saved = accountRepository.save(account);
        
        accountRepository.delete(saved);
        
        AccountPO found = accountRepository.findByName("deletetest");
        assertThat(found).isNull();
    }

    private AccountPO createAccount(String name, ActivationStatus status) {
        AccountPO account = new AccountPO();
        account.setName(name);
        account.setActivationStatus(status);
        account.setCreatedBy(1L);
        account.setUpdatedBy(1L);
        account.setCreateDateTime(LocalDateTime.now());
        account.setUpdatedDateTime(LocalDateTime.now());
        return account;
    }
}