package se.dtime.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ReportDates;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.report.ReportView;
import se.dtime.model.report.UnclosedUser;
import se.dtime.model.report.UnclosedUserReport;
import se.dtime.model.report.UserReport;
import se.dtime.model.timereport.CloseDate;
import se.dtime.model.timereport.Day;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class TimeReportStatusService {

    @Autowired
    private CalendarService calendarService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private CloseDateRepository closeDateRepository;
    @Autowired
    private ReportConverter reportConverter;

    public UnclosedUserReport getCurrentUnclosedUsers() {
        LocalDate date = calendarService.getNowDate();
        return getUnclosedUsersForDate(date);
    }

    public UnclosedUserReport getPreviousUnclosedUsers(LocalDate date) {
        LocalDate reportDate = ReportUtil.getPreviousDate(ReportView.MONTH, date);
        return getUnclosedUsersForDate(reportDate);
    }

    public UnclosedUserReport getNextUnclosedUsers(LocalDate date) {
        LocalDate reportDate = ReportUtil.getNextDate(ReportView.MONTH, date);
        return getUnclosedUsersForDate(reportDate);
    }

    public void closeUserTimeReport(CloseDate closeDate) {
        CloseDatePO closeDatePO = reportConverter.toPO(closeDate);
        closeDateRepository.save(closeDatePO);
    }

    public void openUserTimeReport(CloseDate closeDate) {
        CloseDatePO closeDatePO = reportConverter.toPO(closeDate);
        closeDateRepository.deleteByUserAndDate(closeDatePO.getUser(), closeDatePO.getDate());
    }

    private UnclosedUserReport getUnclosedUsersForDate(LocalDate date) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        ReportDates reportDates = ReportUtil.getReportDates(ReportView.MONTH, date);

        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        int workableHours = calendarService.calcWorkableHours(days);

        List<UserReport> allUserReports = reportRepository.getUserTaskReports(reportDates.getFromDate(), reportDates.getToDate());
        allUserReports.forEach(userReport -> updateClosedReport(userReport, reportDates.getFromDate()));

        // Filter to show only unclosed users (users who haven't closed their reports)
        List<UnclosedUser> unclosedUsers = allUserReports.stream()
                .filter(userReport -> !userReport.isClosed()) // Only unclosed reports
                .map(userReport -> UnclosedUser.builder()
                        .userId(userReport.getUserId())
                        .fullName(userReport.getFullName())
                        .email(userReport.getEmail())
                        .totalTime(userReport.getTotalTime())
                        .closed(userReport.isClosed())
                        .build())
                .sorted((a, b) -> {
                    int nameComparison = a.getFullName().compareToIgnoreCase(b.getFullName());
                    if (nameComparison != 0) return nameComparison;
                    return a.getEmail().compareToIgnoreCase(b.getEmail());
                })
                .toList();

        return UnclosedUserReport.builder()
                .fromDate(reportDates.getFromDate())
                .toDate(reportDates.getToDate())
                .unclosedUsers(unclosedUsers)
                .workableHours(workableHours)
                .build();
    }

    private void updateClosedReport(UserReport userReport, LocalDate fromDate) {
        UserPO userPO = new UserPO(userReport.getUserId());
        LocalDate date = LocalDate.of(fromDate.getYear(), fromDate.getMonthValue(), 1);

        CloseDatePO closeDatePO = closeDateRepository.findByUserAndDate(userPO, date);
        if (closeDatePO != null && closeDatePO.getDate().getYear() == fromDate.getYear() &&
                closeDatePO.getDate().getMonthValue() == fromDate.getMonthValue()) {
            userReport.setClosed(true);
        }
    }
}