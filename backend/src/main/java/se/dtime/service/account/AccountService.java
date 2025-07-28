package se.dtime.service.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.Account;
import se.dtime.model.ActivationStatus;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AccountRepository;

import java.util.List;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountConverter accountConverter;
    @Autowired
    private AccountValidator accountValidator;

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

    public Account get(long id) {
        AccountPO accountPO = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("account.not.found"));
        return accountConverter.toModel(accountPO);
    }

    public void delete(long id) {
        accountValidator.validateDelete(id);
        accountRepository.deleteById(id);
    }
}
