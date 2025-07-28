package se.dtime.service.basis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.model.error.InvalidInputException;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.AccountRepository;

@Service
public class MonthlyCheckValidator {

    @Autowired
    private AccountRepository accountRepository;

    public void validateMonthlyCheck(MonthlyCheck monthlyCheck) {
        accountRepository.findById(monthlyCheck.getAccountId()).orElseThrow(() -> new NotFoundException("account.not.found"));
        if (monthlyCheck.getDate() == null || monthlyCheck.getDate().getDayOfMonth() != 1) {
            throw new InvalidInputException("common.invalid.date");
        }
    }
}
