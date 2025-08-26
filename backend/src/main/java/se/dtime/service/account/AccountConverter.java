package se.dtime.service.account;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.Account;
import se.dtime.service.BaseConverter;

import java.util.List;

@Service
public class AccountConverter extends BaseConverter {

    public Account toModel(AccountPO accountPO) {
        if (accountPO == null) {
            return null;
        }

        return Account.builder().id(accountPO.getId()).
                name(accountPO.getName()).
                activationStatus(accountPO.getActivationStatus()).
                build();
    }

    public AccountPO toPO(Account account) {
        if (account == null) {
            return null;
        }

        AccountPO accountPO = new AccountPO();
        // Only set ID if it's not 0 (for updates)
        // For new entities (ID = 0), let Hibernate generate the ID via sequence
        if (account.getId() != null && account.getId() != 0) {
            accountPO.setId(account.getId());
        }
        accountPO.setName(account.getName());
        accountPO.setActivationStatus(account.getActivationStatus());
        updateBaseData(accountPO);

        return accountPO;
    }

    public Account[] toModel(List<AccountPO> accountPOList) {
        List<Account> accounts = accountPOList.stream()
                .map(this::toModel)
                .toList();
        return accounts.toArray(new Account[0]);
    }
}
