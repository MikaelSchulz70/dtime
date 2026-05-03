package se.dtime.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ReportDates;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.report.*;
import se.dtime.model.timereport.CloseDate;
import se.dtime.model.timereport.Day;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.user.CurrentUserResolver;
import se.dtime.service.user.UserValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ReportService {

    private final UserValidator userValidator;
    private final CurrentUserResolver currentUserResolver;
    private final CalendarService calendarService;
    private final ReportRepository reportRepository;
    private final CloseDateRepository closeDateRepository;
    private final ReportValidator reportValidator;
    private final ReportConverter reportConverter;

    public ReportService(UserValidator userValidator, CurrentUserResolver currentUserResolver, CalendarService calendarService, ReportRepository reportRepository, CloseDateRepository closeDateRepository, ReportValidator reportValidator, ReportConverter reportConverter) {
        this.userValidator = userValidator;
        this.currentUserResolver = currentUserResolver;
        this.calendarService = calendarService;
        this.reportRepository = reportRepository;
        this.closeDateRepository = closeDateRepository;
        this.reportValidator = reportValidator;
        this.reportConverter = reportConverter;
    }

    public Report getCurrentReports(ReportView reportView, ReportType reportType) {
        return getReport(reportView, reportType, calendarService.getNowDate());
    }

    public Report getPreviousReport(ReportView reportView, ReportType reportType, LocalDate date) {
        LocalDate reportDate = ReportUtil.getPreviousDate(reportView, date);
        return getReport(reportView, reportType, reportDate);
    }

    public Report getNextReport(ReportView reportView, ReportType reportType, LocalDate date) {
        LocalDate reportDate = ReportUtil.getNextDate(reportView, date);
        return getReport(reportView, reportType, reportDate);
    }

    public Report getUserReport(ReportView reportView) {
        LocalDate date = calendarService.getNowDate();
        return getUserReport(reportView, date);
    }

    public Report getNextUserReport(ReportView reportView, LocalDate date) {
        LocalDate reportDate = ReportUtil.getNextDate(reportView, date);
        return getUserReport(reportView, reportDate);
    }

    public Report getPreviousUserReport(ReportView reportView, LocalDate date) {
        LocalDate reportDate = ReportUtil.getPreviousDate(reportView, date);
        return getUserReport(reportView, reportDate);
    }

    public void closeTimeReport(CloseDate closeDate) {
        reportValidator.validateCloseTimeReport(closeDate);
        CloseDatePO closeDatePO = reportConverter.toPO(closeDate);
        closeDateRepository.save(closeDatePO);
    }

    public void openTimeReport(CloseDate closeDate) {
        reportValidator.validateOpenTimeReport(closeDate);
        CloseDatePO closeDatePO = reportConverter.toPO(closeDate);
        closeDateRepository.deleteByUserAndDate(closeDatePO.getUser(), closeDatePO.getDate());
    }

    private Report getUserReport(ReportView reportView, LocalDate date) {
        userValidator.validateLoggedIn();
        long userId = currentUserResolver.resolveCurrentUser().getId();
        ReportDates reportDates = ReportUtil.getReportDates(reportView, date);
        return buildUserTaskReport(reportDates, userId);
    }

    private Report getReport(ReportView reportView, ReportType reportType, LocalDate date) {
        ReportDates reportDates = ReportUtil.getReportDates(reportView, date);
        return getReportBetweenDates(reportType, reportDates);
    }

    private Report buildUserTaskReport(ReportDates reportDates, Long scopedUserIdOrNull) {
        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        int workableHours = calendarService.calcWorkableHours(days);

        Report report = new Report(reportDates.getFromDate(), reportDates.getToDate(), workableHours);

        List<UserReport> userReports = scopedUserIdOrNull == null
                ? reportRepository.getUserTaskReports(reportDates.getFromDate(), reportDates.getToDate())
                : reportRepository.getUserTaskReports(scopedUserIdOrNull, reportDates.getFromDate(), reportDates.getToDate());
        userReports.forEach(this::updateClosedReport);
        report.setUserReports(userReports);
        calcStatistics(report, userReports);

        return report;
    }

    private Report getReportBetweenDates(ReportType reportType, ReportDates reportDates) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        if (reportType == ReportType.USER_TASK) {
            return buildUserTaskReport(reportDates, null);
        }

        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        int workableHours = calendarService.calcWorkableHours(days);

        Report report = new Report(reportDates.getFromDate(), reportDates.getToDate(), workableHours);

        if (reportType == ReportType.TASK) {
            List<TaskReport> taskReports = reportRepository.getTaskReports(reportDates.getFromDate(), reportDates.getToDate());
            report.setTaskReports(taskReports);
            taskReports.sort((a, b) -> b.getTotalHours().compareTo(a.getTotalHours()));
        } else if (reportType == ReportType.USER) {
            List<UserReport> userReports = reportRepository.getUserReports(reportDates.getFromDate(), reportDates.getToDate());
            report.setUserReports(userReports);
        } else if (reportType == ReportType.ACCOUNT) {
            List<AccountReport> accountReports = reportRepository.getAccountReports(reportDates.getFromDate(), reportDates.getToDate());
            report.setAccountReports(accountReports);
        } else if (reportType == ReportType.BILLABLE_TASK_TYPE) {
            // For billable task type report, we don't need to populate the report object
            // as it will be handled by the dedicated endpoint
        }

        return report;
    }

    void calcStatistics(Report report, List<UserReport> userReports) {
        int totalWorkableHours = report.getWorkableHours() * userReports.size();
        report.setTotalWorkableHours(totalWorkableHours);

        BigDecimal totalHoursWorked = userReports.stream()
                .map(UserReport::getTotalTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalHoursWorked(totalHoursWorked);
    }

    private void updateClosedReport(UserReport userReport) {
        UserPO userPO = new UserPO(userReport.getUserId());
        LocalDate date = LocalDate.of(userReport.getFromDate().getYear(), userReport.getFromDate().getMonthValue(), 1);

        CloseDatePO closeDatePO = closeDateRepository.findByUserAndDate(userPO, date);
        if (closeDatePO != null && closeDatePO.getDate().getYear() == userReport.getFromDate().getYear() &&
                closeDatePO.getDate().getMonthValue() == userReport.getFromDate().getMonthValue()) {
            userReport.setClosed(true);
        }
    }


}
