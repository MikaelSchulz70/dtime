package se.dtime.service.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.AccountRepository;
import se.dtime.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AccountValidatorTest {
    @InjectMocks
    private AccountValidator accountValidator;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TaskRepository taskRepository;


    @BeforeEach
    public void setup() {
        accountValidator.init();
    }

    @Test
    public void validateUserNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(AccountValidator.FIELD_NAME).value("test1").build();
        accountValidator.validate(attribute);
    }

    @Test
    public void validateAddNameExists() {
        when(accountRepository.findByName("test")).thenReturn(new AccountPO(2L));
        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validateAdd(Account.builder().id(1L).name("test").build()));
        assertThat(exception.getMessage()).isEqualTo("account.name.not.unique");
    }

    @Test
    public void validateUpdateNotFound() {
        when(accountRepository.existsById(1L)).thenReturn(false);
        NotFoundException exception = assertThrows(NotFoundException.class, () -> accountValidator.validateUpdate(Account.builder().id(1L).build()));
        assertThat(exception.getMessage()).isEqualTo("account.not.found");
    }

    @Test
    public void validateUpdateNameExists() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByName("test")).thenReturn(new AccountPO(2L));
        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validateUpdate(Account.builder().id(1L).name("test").build()));
        assertThat(exception.getMessage()).isEqualTo("account.name.not.unique");
    }

    @Test
    public void validateUpdateNameInactive() {
        when(accountRepository.existsById(1L)).thenReturn(true);

        TaskPO taskPO = new TaskPO(3L);
        taskPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskRepository.findByAccount(any(AccountPO.class))).thenReturn(List.of(taskPO));
        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validateUpdate(Account.builder().id(1L).activationStatus(ActivationStatus.INACTIVE).build()));
        assertThat(exception.getMessage()).isEqualTo("account.inactivation.not.allowed");
    }

    @Test
    public void validateUserNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(AccountValidator.FIELD_NAME).value("").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validate(attribute));
        assertThat(exception.getMessage()).isEqualTo("account.name.length");
    }

    @Test
    public void validateUserNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(AccountValidator.FIELD_NAME).value("01234567890123456789012345678901234567890").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validate(attribute));
        assertThat(exception.getMessage()).isEqualTo("account.name.length");
    }

    @Disabled("TODO fix this test")
    @Test
    public void validateNameNotUnique() {
        AccountPO accountPO = new AccountPO();
        accountPO.setId(2L);
        when(accountRepository.findByName("test")).thenReturn(accountPO);

        Attribute attribute = Attribute.builder().id(1).name(AccountValidator.FIELD_NAME).value("test").build();
        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validate(attribute));
        assertThat(exception.getMessage()).isEqualTo("account.name.not.unique");
    }

    @Test
    public void validateDeleteNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> accountValidator.validateDelete(1L));
        assertThat(exception.getMessage()).isEqualTo("account.not.found");
    }

    @Test
    public void validateDeleteHasActiveTasks() {
        AccountPO accountPO = new AccountPO(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountPO));
        when(taskRepository.findByAccount(accountPO)).thenReturn(List.of(new TaskPO(1L)));

        ValidationException exception = assertThrows(ValidationException.class, () -> accountValidator.validateDelete(1L));
        assertThat(exception.getMessage()).isEqualTo("account.cannot.delete.account.with.tasks");
    }
}