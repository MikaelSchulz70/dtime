package se.dtime.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.ReportDates;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.report.UnclosedUser;
import se.dtime.model.report.UnclosedUserReport;
import se.dtime.model.report.UserReport;
import se.dtime.model.timereport.CloseDate;
import se.dtime.model.timereport.Day;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.UserRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.period.PeriodResolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TimeReportStatusService {

    private final CalendarService calendarService;
    private final ReportRepository reportRepository;
    private final CloseDateRepository closeDateRepository;
    private final UserRepository userRepository;
    private final ReportConverter reportConverter;
    private final PeriodResolver periodResolver;

    public TimeReportStatusService(CalendarService calendarService, ReportRepository reportRepository, CloseDateRepository closeDateRepository, UserRepository userRepository, ReportConverter reportConverter, PeriodResolver periodResolver) {
        this.calendarService = calendarService;
        this.reportRepository = reportRepository;
        this.closeDateRepository = closeDateRepository;
        this.userRepository = userRepository;
        this.reportConverter = reportConverter;
        this.periodResolver = periodResolver;
    }

    public UnclosedUserReport getUnclosedUsersForCurrentMonth() {
        LocalDate now = calendarService.getNowDate();
        return getUnclosedUsersForMonth(now.getYear(), now.getMonthValue());
    }

    public UnclosedUserReport getUnclosedUsersForMonth(int year, int month) {
        return getUsersForMonth(year, month, false);
    }

    public UnclosedUserReport getClosedUsersForMonth(int year, int month) {
        return getUsersForMonth(year, month, true);
    }

    public UnclosedUserReport getUsersForMonth(int year, int month, boolean closedOnly) {
        ReportDates reportDates = periodResolver.resolveMonth(year, month);
        return getUsersForReportDates(reportDates, closedOnly);
    }

    public void closeUserTimeReport(CloseDate closeDate) {
        CloseDatePO closeDatePO = reportConverter.toPO(closeDate);
        closeDateRepository.save(closeDatePO);
    }

    public void openUserTimeReport(CloseDate closeDate) {
        CloseDatePO closeDatePO = reportConverter.toPO(closeDate);
        closeDateRepository.deleteByUserAndDate(closeDatePO.getUser(), closeDatePO.getDate());
    }

    private UnclosedUserReport getUsersForReportDates(ReportDates reportDates, boolean closedOnly) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        int workableHours = calendarService.calcWorkableHours(days);

        List<UserReport> allUserReports = loadUserReportsForStatus(reportDates);
        allUserReports.forEach(userReport -> updateClosedReport(userReport, reportDates.getFromDate()));

        List<UnclosedUser> unclosedUsers = allUserReports.stream()
                .filter(userReport -> closedOnly ? userReport.isClosed() : !userReport.isClosed())
                .map(userReport -> UnclosedUser.builder()
                        .userId(userReport.getUserId())
                        .fullName(userReport.getFullName())
                        .email(userReport.getEmail())
                        .totalTime(userReport.getTotalTime())
                        .closed(userReport.isClosed())
                        .build())
                .sorted((a, b) -> {
                    String aName = a.getFullName() != null ? a.getFullName() : "";
                    String bName = b.getFullName() != null ? b.getFullName() : "";
                    int nameComparison = aName.compareToIgnoreCase(bName);
                    if (nameComparison != 0) return nameComparison;
                    String aEmail = a.getEmail() != null ? a.getEmail() : "";
                    String bEmail = b.getEmail() != null ? b.getEmail() : "";
                    return aEmail.compareToIgnoreCase(bEmail);
                })
                .toList();

        return UnclosedUserReport.builder()
                .fromDate(reportDates.getFromDate())
                .toDate(reportDates.getToDate())
                .unclosedUsers(unclosedUsers)
                .workableHours(workableHours)
                .build();
    }

    private List<UserReport> loadUserReportsForStatus(ReportDates reportDates) {
        Map<Long, UserReport> byUserId = new LinkedHashMap<>();
        for (UserReport userReport : reportRepository.getUserTaskReports(reportDates.getFromDate(), reportDates.getToDate())) {
            byUserId.put(userReport.getUserId(), userReport);
        }
        for (UserPO userPO : userRepository.findByActivationStatusOrderByDisplayNameAsc(ActivationStatus.ACTIVE)) {
            byUserId.computeIfAbsent(userPO.getId(), id -> toUserReport(userPO, reportDates));
        }
        return new ArrayList<>(byUserId.values());
    }

    private UserReport toUserReport(UserPO userPO, ReportDates reportDates) {
        UserReport userReport = new UserReport();
        userReport.setUserId(userPO.getId());
        userReport.setFullName(userPO.getFullName());
        userReport.setEmail(userPO.getEmail());
        userReport.setFromDate(reportDates.getFromDate());
        userReport.setToDate(reportDates.getToDate());
        return userReport;
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
