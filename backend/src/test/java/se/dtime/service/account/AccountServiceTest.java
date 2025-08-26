package se.dtime.service.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AccountRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountConverter accountConverter;
    @Mock
    private AccountValidator accountValidator;

    private Account testAccount;
    private AccountPO testAccountPO;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .name("Test Account")
                .activationStatus(ActivationStatus.ACTIVE)
                .build();

        testAccountPO = new AccountPO();
        testAccountPO.setId(1L);
        testAccountPO.setName("Test Account");
        testAccountPO.setActivationStatus(ActivationStatus.ACTIVE);
    }

    @Test
    void add_ValidAccount_ShouldReturnSavedAccount() {
        // Given
        when(accountConverter.toPO(testAccount)).thenReturn(testAccountPO);
        when(accountRepository.save(testAccountPO)).thenReturn(testAccountPO);
        when(accountConverter.toModel(testAccountPO)).thenReturn(testAccount);

        // When
        Account result = accountService.add(testAccount);

        // Then
        assertThat(result).isEqualTo(testAccount);
        verify(accountValidator).validateAdd(testAccount);
        verify(accountRepository).save(testAccountPO);
    }

    @Test
    void update_ValidAccount_ShouldUpdateAccount() {
        // Given
        when(accountConverter.toPO(testAccount)).thenReturn(testAccountPO);

        // When
        assertDoesNotThrow(() -> accountService.update(testAccount));

        // Then
        verify(accountValidator).validateUpdate(testAccount);
        verify(accountRepository).save(testAccountPO);
    }

    @Test
    void getAll_WithoutFilter_ShouldReturnAllAccounts() {
        // Given
        List<AccountPO> accountPOs = Arrays.asList(testAccountPO);
        Account[] expectedAccounts = {testAccount};

        when(accountRepository.findAll(any(Sort.class))).thenReturn(accountPOs);
        when(accountConverter.toModel(accountPOs)).thenReturn(expectedAccounts);

        // When
        Account[] result = accountService.getAll(null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testAccount);
        verify(accountRepository).findAll(any(Sort.class));
    }

    @Test
    void getAll_ActiveAccountsOnly_ShouldReturnActiveAccounts() {
        // Given
        List<AccountPO> activeAccounts = Collections.singletonList(testAccountPO);
        Account[] expectedAccounts = {testAccount};

        when(accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.ACTIVE))
                .thenReturn(activeAccounts);
        when(accountConverter.toModel(activeAccounts)).thenReturn(expectedAccounts);

        // When
        Account[] result = accountService.getAll(true);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(testAccount);
        verify(accountRepository).findByActivationStatusOrderByNameAsc(ActivationStatus.ACTIVE);
    }

    @Test
    void getAll_InactiveAccountsOnly_ShouldReturnInactiveAccounts() {
        // Given
        AccountPO inactiveAccountPO = new AccountPO();
        inactiveAccountPO.setActivationStatus(ActivationStatus.INACTIVE);

        Account inactiveAccount = Account
                .builder()
                .activationStatus(ActivationStatus.INACTIVE)
                .build();

        List<AccountPO> inactiveAccounts = Collections.singletonList(inactiveAccountPO);
        Account[] expectedAccounts = {inactiveAccount};

        when(accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.INACTIVE))
                .thenReturn(inactiveAccounts);
        when(accountConverter.toModel(inactiveAccounts)).thenReturn(expectedAccounts);

        // When
        Account[] result = accountService.getAll(false);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(inactiveAccount);
        verify(accountRepository).findByActivationStatusOrderByNameAsc(ActivationStatus.INACTIVE);
    }

    @Test
    void get_ValidAccountId_ShouldReturnAccount() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccountPO));
        when(accountConverter.toModel(testAccountPO)).thenReturn(testAccount);

        // When
        Account result = accountService.get(1L);

        // Then
        assertThat(result).isEqualTo(testAccount);
        verify(accountRepository).findById(1L);
    }

    @Test
    void get_NonExistentAccountId_ShouldThrowNotFoundException() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NotFoundException.class, () -> accountService.get(999L));
    }

    @Test
    void delete_ValidAccountId_ShouldDeleteAccount() {
        // When
        assertDoesNotThrow(() -> accountService.delete(1L));

        // Then
        verify(accountValidator).validateDelete(1L);
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void add_AccountWithLongName_ShouldPassValidation() {
        // Given
        Account longNameAccount = Account.builder()
                .name("A very long account name that should still be valid")
                .build();

        AccountPO longNameAccountPO = new AccountPO();
        longNameAccountPO.setName("A very long account name that should still be valid");

        when(accountConverter.toPO(longNameAccount)).thenReturn(longNameAccountPO);
        when(accountRepository.save(longNameAccountPO)).thenReturn(longNameAccountPO);
        when(accountConverter.toModel(longNameAccountPO)).thenReturn(longNameAccount);

        // When
        Account result = accountService.add(longNameAccount);

        // Then
        assertThat(result.getName()).isEqualTo("A very long account name that should still be valid");
        verify(accountValidator).validateAdd(longNameAccount);
    }

    @Test
    void update_InactiveAccount_ShouldUpdateSuccessfully() {
        // Given
        Account inactiveAccount = Account.builder()
                .activationStatus(ActivationStatus.INACTIVE)
                .build();

        AccountPO inactiveAccountPO = new AccountPO();
        inactiveAccountPO.setActivationStatus(ActivationStatus.INACTIVE);

        when(accountConverter.toPO(inactiveAccount)).thenReturn(inactiveAccountPO);

        // When
        assertDoesNotThrow(() -> accountService.update(inactiveAccount));

        // Then
        verify(accountValidator).validateUpdate(inactiveAccount);
        verify(accountRepository).save(inactiveAccountPO);
    }

    @Test
    void getAll_EmptyRepository_ShouldReturnEmptyArray() {
        // Given
        when(accountRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());
        when(accountConverter.toModel(Collections.emptyList())).thenReturn(new Account[0]);

        // When
        Account[] result = accountService.getAll(null);

        // Then
        assertThat(result).isEmpty();
        verify(accountRepository).findAll(any(Sort.class));
    }
}