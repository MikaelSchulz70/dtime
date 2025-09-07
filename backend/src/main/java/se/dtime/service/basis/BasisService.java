package se.dtime.service.basis;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.MonthlyCheckPO;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.repository.MonthlyCheckRepository;

@Service
public class BasisService {

    private final MonthlyCheckRepository monthlyCheckRepository;
    private final MonthlyCheckConverter monthlyCheckConverter;
    private final MonthlyCheckValidator monthlyCheckValidator;

    public BasisService(MonthlyCheckRepository monthlyCheckRepository, MonthlyCheckConverter monthlyCheckConverter, MonthlyCheckValidator monthlyCheckValidator) {
        this.monthlyCheckRepository = monthlyCheckRepository;
        this.monthlyCheckConverter = monthlyCheckConverter;
        this.monthlyCheckValidator = monthlyCheckValidator;
    }

    public MonthlyCheck addUpdateMonthlyCheck(MonthlyCheck monthlyCheck) {
        monthlyCheckValidator.validateMonthlyCheck(monthlyCheck);
        MonthlyCheckPO monthlyCheckPO = monthlyCheckRepository.findByAccountAndDate(new AccountPO(monthlyCheck.getAccountId()), monthlyCheck.getDate());
        if (monthlyCheckPO == null) {
            monthlyCheckPO = monthlyCheckConverter.toPO(monthlyCheck);
        } else {
            // Update existing entity, preserving the database ID
            monthlyCheckPO.setDate(monthlyCheck.getDate());
            monthlyCheckConverter.updateBaseData(monthlyCheckPO);
        }
        return monthlyCheckConverter.toModel(monthlyCheckRepository.save(monthlyCheckPO));
    }
}
