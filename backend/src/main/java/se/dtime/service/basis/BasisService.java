package se.dtime.service.basis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.MonthlyCheckPO;
import se.dtime.model.ReportDates;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.repository.MonthlyCheckRepository;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;

@Service
public class BasisService {
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private MonthlyCheckRepository monthlyCheckRepository;
    @Autowired
    private MonthlyCheckConverter monthlyCheckConverter;
    @Autowired
    private MonthlyCheckValidator monthlyCheckValidator;

    LocalDate getNextDate(LocalDate date) {
        return date.plusMonths(1);
    }

    LocalDate getPreviousDate(LocalDate date) {
        return date.minusMonths(1);
    }

    ReportDates getReportDates(LocalDate date) {
        LocalDate fromDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate toDate = date.withDayOfMonth(date.lengthOfMonth());
        return new ReportDates(fromDate, toDate);
    }

    public MonthlyCheck addUpdateMonthlyCheck(MonthlyCheck monthlyCheck) {
        monthlyCheckValidator.validateMonthlyCheck(monthlyCheck);
        MonthlyCheckPO monthlyCheckPO = monthlyCheckRepository.findByAccountAndDate(new AccountPO(monthlyCheck.getAccountId()), monthlyCheck.getDate());
        if (monthlyCheckPO != null) {
            monthlyCheckPO = monthlyCheckConverter.toPO(monthlyCheck);
        }
        return monthlyCheckConverter.toModel(monthlyCheckRepository.save(monthlyCheckPO));
    }
}
