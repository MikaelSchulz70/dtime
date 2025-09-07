package se.dtime.service.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.PagedResponse;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AccountRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountConverter accountConverter;
    private final AccountValidator accountValidator;

    public AccountService(AccountRepository accountRepository, AccountConverter accountConverter, AccountValidator accountValidator) {
        this.accountRepository = accountRepository;
        this.accountConverter = accountConverter;
        this.accountValidator = accountValidator;
    }

    public Account add(Account account) {
        accountValidator.validateAdd(account);
        AccountPO accountPO = accountConverter.toPO(account);
        AccountPO savedPO = accountRepository.save(accountPO);
        return accountConverter.toModel(savedPO);
    }

    public void update(Account account) {
        accountValidator.validateUpdate(account);
        AccountPO accountPO = accountConverter.toPO(account);
        accountRepository.save(accountPO);
    }

    public Account[] getAll(Boolean active) {
        List<AccountPO> accountPOS;

        if (active == null) {
            accountPOS = accountRepository.findAll(Sort.by("name").ascending());
        } else if (active) {
            accountPOS = accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.ACTIVE);
        } else {
            accountPOS = accountRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.INACTIVE);
        }

        return accountConverter.toModel(accountPOS);
    }

    public PagedResponse<Account> getAllPaged(Pageable pageable, Boolean active, String name) {
        Page<AccountPO> page;

        if (active != null) {
            ActivationStatus activationStatus = active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE;
            page = accountRepository.findByActivationStatus(pageable, activationStatus);
        } else {
            page = accountRepository.findAll(pageable);
        }

        // Apply name filter if provided
        List<AccountPO> filteredAccounts = page.getContent();
        if (name != null && !name.isEmpty()) {
            filteredAccounts = page.getContent().stream()
                    .filter(a -> a.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        Account[] accounts = accountConverter.toModel(filteredAccounts);

        return new PagedResponse<>(
                Arrays.asList(accounts),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }

    public Account get(long id) {
        AccountPO accountPO = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("account.not.found"));
        return accountConverter.toModel(accountPO);
    }

    public void delete(long id) {
        accountValidator.validateDelete(id);
        accountRepository.deleteById(id);
    }
}
