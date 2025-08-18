package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.MonthlyCheckPO;
import se.dtime.model.ActivationStatus;

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
class MonthlyCheckRepositoryIT {

    @Autowired
    private MonthlyCheckRepository monthlyCheckRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldSaveAndFindMonthlyCheck() {
        AccountPO account = createAndSaveAccount("Test Account");
        LocalDate checkDate = LocalDate.of(2024, 12, 31);
        
        MonthlyCheckPO monthlyCheck = createMonthlyCheck(account, checkDate, true, false);
        
        MonthlyCheckPO saved = monthlyCheckRepository.save(monthlyCheck);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAccount().getId()).isEqualTo(account.getId());
        assertThat(saved.getDate()).isEqualTo(checkDate);
        assertThat(saved.isInvoiceVerified()).isTrue();
        assertThat(saved.isInvoiceSent()).isFalse();
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByAccountAndDate() {
        AccountPO account = createAndSaveAccount("Test Account");
        LocalDate checkDate = LocalDate.of(2024, 6, 30);
        
        MonthlyCheckPO monthlyCheck = createMonthlyCheck(account, checkDate, false, true);
        monthlyCheckRepository.save(monthlyCheck);
        
        MonthlyCheckPO found = monthlyCheckRepository.findByAccountAndDate(account, checkDate);
        
        assertThat(found).isNotNull();
        assertThat(found.getAccount().getId()).isEqualTo(account.getId());
        assertThat(found.getDate()).isEqualTo(checkDate);
        assertThat(found.isInvoiceVerified()).isFalse();
        assertThat(found.isInvoiceSent()).isTrue();
    }

    @Test
    void shouldReturnNullWhenMonthlyCheckNotFoundByAccountAndDate() {
        AccountPO account = createAndSaveAccount("Test Account");
        LocalDate nonExistentDate = LocalDate.of(2024, 1, 1);
        
        MonthlyCheckPO found = monthlyCheckRepository.findByAccountAndDate(account, nonExistentDate);
        
        assertThat(found).isNull();
    }

    @Test
    void shouldHandleMultipleMonthlyChecksForDifferentAccounts() {
        AccountPO account1 = createAndSaveAccount("Account 1");
        AccountPO account2 = createAndSaveAccount("Account 2");
        LocalDate sameDate = LocalDate.of(2024, 12, 31);
        
        MonthlyCheckPO check1 = createMonthlyCheck(account1, sameDate, true, true);
        MonthlyCheckPO check2 = createMonthlyCheck(account2, sameDate, false, false);
        
        monthlyCheckRepository.save(check1);
        monthlyCheckRepository.save(check2);
        
        MonthlyCheckPO found1 = monthlyCheckRepository.findByAccountAndDate(account1, sameDate);
        MonthlyCheckPO found2 = monthlyCheckRepository.findByAccountAndDate(account2, sameDate);
        
        assertThat(found1).isNotNull();
        assertThat(found1.getAccount().getId()).isEqualTo(account1.getId());
        assertThat(found1.isInvoiceVerified()).isTrue();
        assertThat(found1.isInvoiceSent()).isTrue();
        
        assertThat(found2).isNotNull();
        assertThat(found2.getAccount().getId()).isEqualTo(account2.getId());
        assertThat(found2.isInvoiceVerified()).isFalse();
        assertThat(found2.isInvoiceSent()).isFalse();
    }

    @Test
    void shouldHandleMultipleMonthlyChecksForSameAccount() {
        AccountPO account = createAndSaveAccount("Multi Check Account");
        
        LocalDate date1 = LocalDate.of(2024, 1, 31);
        LocalDate date2 = LocalDate.of(2024, 2, 29);
        LocalDate date3 = LocalDate.of(2024, 3, 31);
        
        MonthlyCheckPO check1 = createMonthlyCheck(account, date1, true, false);
        MonthlyCheckPO check2 = createMonthlyCheck(account, date2, false, true);
        MonthlyCheckPO check3 = createMonthlyCheck(account, date3, true, true);
        
        monthlyCheckRepository.save(check1);
        monthlyCheckRepository.save(check2);
        monthlyCheckRepository.save(check3);
        
        MonthlyCheckPO found1 = monthlyCheckRepository.findByAccountAndDate(account, date1);
        MonthlyCheckPO found2 = monthlyCheckRepository.findByAccountAndDate(account, date2);
        MonthlyCheckPO found3 = monthlyCheckRepository.findByAccountAndDate(account, date3);
        
        assertThat(found1.isInvoiceVerified()).isTrue();
        assertThat(found1.isInvoiceSent()).isFalse();
        
        assertThat(found2.isInvoiceVerified()).isFalse();
        assertThat(found2.isInvoiceSent()).isTrue();
        
        assertThat(found3.isInvoiceVerified()).isTrue();
        assertThat(found3.isInvoiceSent()).isTrue();
    }

    @Test
    void shouldUpdateMonthlyCheck() {
        AccountPO account = createAndSaveAccount("Update Account");
        LocalDate checkDate = LocalDate.of(2024, 12, 31);
        
        MonthlyCheckPO monthlyCheck = createMonthlyCheck(account, checkDate, false, false);
        MonthlyCheckPO saved = monthlyCheckRepository.save(monthlyCheck);
        
        saved.setInvoiceVerified(true);
        saved.setInvoiceSent(true);
        saved.setDate(LocalDate.of(2025, 1, 31));
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());
        
        MonthlyCheckPO updated = monthlyCheckRepository.save(saved);
        
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.isInvoiceVerified()).isTrue();
        assertThat(updated.isInvoiceSent()).isTrue();
        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldDeleteMonthlyCheck() {
        AccountPO account = createAndSaveAccount("Delete Account");
        LocalDate checkDate = LocalDate.of(2024, 12, 31);
        
        MonthlyCheckPO monthlyCheck = createMonthlyCheck(account, checkDate, true, true);
        MonthlyCheckPO saved = monthlyCheckRepository.save(monthlyCheck);
        
        monthlyCheckRepository.delete(saved);
        
        MonthlyCheckPO found = monthlyCheckRepository.findByAccountAndDate(account, checkDate);
        assertThat(found).isNull();
    }

    @Test
    void shouldFindAllMonthlyChecks() {
        AccountPO account1 = createAndSaveAccount("Account 1");
        AccountPO account2 = createAndSaveAccount("Account 2");
        
        MonthlyCheckPO check1 = createMonthlyCheck(account1, LocalDate.of(2024, 6, 30), true, false);
        MonthlyCheckPO check2 = createMonthlyCheck(account2, LocalDate.of(2024, 12, 31), false, true);
        
        monthlyCheckRepository.save(check1);
        monthlyCheckRepository.save(check2);
        
        List<MonthlyCheckPO> allChecks = monthlyCheckRepository.findAll();
        
        assertThat(allChecks).hasSize(2);
        assertThat(allChecks).extracting(mc -> mc.getAccount().getName())
            .containsExactlyInAnyOrder("Account 1", "Account 2");
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        MonthlyCheckPO check1 = new MonthlyCheckPO(1L);
        MonthlyCheckPO check2 = new MonthlyCheckPO(1L);
        MonthlyCheckPO check3 = new MonthlyCheckPO(2L);
        
        assertThat(check1).isEqualTo(check2);
        assertThat(check1).isNotEqualTo(check3);
        assertThat(check1.hashCode()).isEqualTo(check2.hashCode());
        assertThat(check1.hashCode()).isNotEqualTo(check3.hashCode());
    }

    @Test
    void shouldHandleBooleanFields() {
        AccountPO account = createAndSaveAccount("Boolean Test Account");
        LocalDate checkDate = LocalDate.of(2024, 12, 31);
        
        // Test all combinations of boolean values
        MonthlyCheckPO checkTrueFalse = createMonthlyCheck(account, checkDate, true, false);
        MonthlyCheckPO savedTrueFalse = monthlyCheckRepository.save(checkTrueFalse);
        
        assertThat(savedTrueFalse.isInvoiceVerified()).isTrue();
        assertThat(savedTrueFalse.isInvoiceSent()).isFalse();
        
        // Update to test false/true combination
        savedTrueFalse.setInvoiceVerified(false);
        savedTrueFalse.setInvoiceSent(true);
        MonthlyCheckPO updatedFalseTrue = monthlyCheckRepository.save(savedTrueFalse);
        
        assertThat(updatedFalseTrue.isInvoiceVerified()).isFalse();
        assertThat(updatedFalseTrue.isInvoiceSent()).isTrue();
    }

    @Test
    void shouldCountMonthlyChecks() {
        AccountPO account1 = createAndSaveAccount("Count Account 1");
        AccountPO account2 = createAndSaveAccount("Count Account 2");
        
        MonthlyCheckPO check1 = createMonthlyCheck(account1, LocalDate.of(2024, 1, 31), true, false);
        MonthlyCheckPO check2 = createMonthlyCheck(account1, LocalDate.of(2024, 2, 29), false, true);
        MonthlyCheckPO check3 = createMonthlyCheck(account2, LocalDate.of(2024, 3, 31), true, true);
        
        monthlyCheckRepository.save(check1);
        monthlyCheckRepository.save(check2);
        monthlyCheckRepository.save(check3);
        
        long count = monthlyCheckRepository.count();
        
        assertThat(count).isEqualTo(3);
    }

    private MonthlyCheckPO createMonthlyCheck(AccountPO account, LocalDate date, boolean invoiceVerified, boolean invoiceSent) {
        MonthlyCheckPO monthlyCheck = new MonthlyCheckPO();
        monthlyCheck.setAccount(account);
        monthlyCheck.setDate(date);
        monthlyCheck.setInvoiceVerified(invoiceVerified);
        monthlyCheck.setInvoiceSent(invoiceSent);
        monthlyCheck.setCreatedBy(1L);
        monthlyCheck.setUpdatedBy(1L);
        monthlyCheck.setCreateDateTime(LocalDateTime.now());
        monthlyCheck.setUpdatedDateTime(LocalDateTime.now());
        return monthlyCheck;
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
}