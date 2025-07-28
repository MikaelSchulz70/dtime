package se.dtime.service.account;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.Account;
import se.dtime.service.BaseConverter;

import java.util.List;
import java.util.stream.Collectors;

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
        accountPO.setId(account.getId());
        accountPO.setName(account.getName());
        accountPO.setActivationStatus(account.getActivationStatus());
        updateBaseData(accountPO);

        return accountPO;
    }

    public Account[] toModel(List<AccountPO> accountPOList) {
        List<Account> accounts = accountPOList.stream().map(o -> toModel(o)).collect(Collectors.toList());
        return accounts.toArray(new Account[accounts.size()]);
    }
}
