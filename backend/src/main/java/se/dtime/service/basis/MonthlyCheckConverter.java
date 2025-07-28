package se.dtime.service.basis;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.MonthlyCheckPO;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.service.BaseConverter;

@Service
public class MonthlyCheckConverter extends BaseConverter {

    public MonthlyCheck toModel(MonthlyCheckPO monthlyCheckPO) {
        if (monthlyCheckPO == null) {
            return null;
        }

        return MonthlyCheck.builder().id(monthlyCheckPO.getId()).
                accountId(monthlyCheckPO.getAccount().getId()).
                date(monthlyCheckPO.getDate()).
                build();
    }

    public MonthlyCheckPO toPO(MonthlyCheck monthlyCheck) {
        if (monthlyCheck == null) {
            return null;
        }

        MonthlyCheckPO monthlyCheckPO = new MonthlyCheckPO();
        // Only set ID if it's not 0 (for updates)
        // For new entities (ID = 0), let Hibernate generate the ID via sequence
        if (monthlyCheck.getId() != null && monthlyCheck.getId() != 0) {
            monthlyCheckPO.setId(monthlyCheck.getId());
        }
        monthlyCheckPO.setAccount(new AccountPO(monthlyCheck.getAccountId()));
        monthlyCheckPO.setDate(monthlyCheck.getDate());
        updateBaseData(monthlyCheckPO);
        return monthlyCheckPO;
    }
}
