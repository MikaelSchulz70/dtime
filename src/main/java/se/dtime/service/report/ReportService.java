package se.dtime.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ReportDates;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.report.*;
import se.dtime.model.timereport.CloseDate;
import se.dtime.model.timereport.Day;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TimeReportRepository;
import se.dtime.repository.jdbc.ReportRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.system.SystemProperty;
import se.dtime.service.user.UserValidator;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ReportService {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Autowired
    private TimeReportRepository timeReportRepository;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private CloseDateRepository closeDateRepository;
    @Autowired
    private ReportValidator reportValidator;
    @Autowired
    private ReportConverter reportConverter;
    @Autowired
    private Environment environment;

    private static Float provisionLimit = null;

    @PostConstruct
    public void setUp() {
        if (provisionLimit == null) {
            String startDate = environment.getProperty(SystemProperty.PROVISION_LIMIT_PROP);
            if (StringUtils.isEmpty(startDate)) {
                log.error("System provision limit not configured");
            }

            provisionLimit = Float.parseFloat(startDate);
        }
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

        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userExt == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        ReportDates reportDates = ReportUtil.getReportDates(reportView, date);
        return getUserReportBetweenDates(userExt.getId(), reportDates);
    }

    private Report getReport(ReportView reportView, ReportType reportType, LocalDate date) {
        ReportDates reportDates = ReportUtil.getReportDates(reportView, date);
        return getReportBetweenDates(reportType, reportDates);
    }

    private Report getUserReportBetweenDates(long idUser, ReportDates reportDates) {
        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        int workableHours = calendarService.calcWorkableHours(days);

        Report report = new Report(reportDates.getFromDate(), reportDates.getToDate(), workableHours);

        List<UserReport> userReports = reportRepository.getUserProjectReports(idUser, reportDates.getFromDate(), reportDates.getToDate());
        userReports.forEach(this::updateClosedReport);
        userReports.forEach(u -> calcAndUpdateProvisionHours(workableHours, u));
        report.setUserReports(userReports);
        calcStatistics(report, userReports);

        return report;
    }

    private Report getReportBetweenDates(ReportType reportType, ReportDates reportDates) {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userExt == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        int workableHours = calendarService.calcWorkableHours(days);

        Report report = new Report(reportDates.getFromDate(), reportDates.getToDate(), workableHours);

        if (reportType == ReportType.USER_PROJECT) {
            List<UserReport> userReports = reportRepository.getUserProjectReports(reportDates.getFromDate(), reportDates.getToDate());
            userReports.forEach(this::updateClosedReport);
            userReports.forEach(u -> calcAndUpdateProvisionHours(workableHours, u));
            report.setUserReports(userReports);
            calcStatistics(report, userReports);
        } else if (reportType == ReportType.PROJECT) {
            List<ProjectReport> projectReports = reportRepository.getProjectsReports(reportDates.getFromDate(), reportDates.getToDate());
            report.setProjectReports(projectReports);
            projectReports.sort((a, b) -> Double.compare(b.getTotalHours(), a.getTotalHours()));
        } else if (reportType == ReportType.USER) {
            List<UserReport> userReports = reportRepository.getUserReports(reportDates.getFromDate(), reportDates.getToDate());
            report.setUserReports(userReports);
        } else if (reportType == ReportType.PROJECT_CATEGORY) {
            List<ProjectCategoryReport> projectsCategoryReports = reportRepository.getProjectsCategoryReports(reportDates.getFromDate(), reportDates.getToDate());
            report.setProjectCategoryReports(projectsCategoryReports);
            projectsCategoryReports.sort((a, b) -> Double.compare(b.getTotalHours(), a.getTotalHours()));
        } else if (reportType == ReportType.PROJECT_USER) {
            List<ProjectUserReport> projectUserReports = reportRepository.getProjectUserReport(reportDates.getFromDate(), reportDates.getToDate());
            report.setProjectUserReports(projectUserReports);
            projectUserReports.sort((a, b) -> Double.compare(b.getTotalHours(), a.getTotalHours()));
        }

        return report;
    }

    void calcStatistics(Report report, List<UserReport> userReports) {
        int totalWorkableHours = report.getWorkableHours() * userReports.size();
        report.setTotalWorkableHours(totalWorkableHours);

        double totalWorkedExternalProvision = userReports.stream().mapToDouble(UserReport::getTotalTimeExternalProvision).sum();
        report.setTotalWorkedHoursExternalProvision(totalWorkedExternalProvision);

        double totalWorkedExternalNoProvision = userReports.stream().mapToDouble(UserReport::getTotalTimeExternalNoProvision).sum();
        report.setTotalWorkedHoursExternalNoProvision(totalWorkedExternalNoProvision);

        double totalWorkedInternalProvision = userReports.stream().mapToDouble(UserReport::getTotalTimeInternalProvision).sum();
        report.setTotalWorkedHoursInternalProvision(totalWorkedInternalProvision);

        double totalWorkedInternalNoProvision = userReports.stream().mapToDouble(UserReport::getTotalTimeInternalNoProvision).sum();
        report.setTotalWorkedHoursInternalNoProvision(totalWorkedInternalNoProvision);

        double totalWorkedHoursOnCall = userReports.stream().mapToDouble(UserReport::getTotalTimeOnCall).sum();
        report.setTotalWorkedHoursOnCall(totalWorkedHoursOnCall);
    }

    private void updateClosedReport(UserReport userReport) {
        UserPO userPO = new UserPO(userReport.getIdUser());
        LocalDate date = LocalDate.of(userReport.getFromDate().getYear(), userReport.getFromDate().getMonthValue(), 1);

        CloseDatePO closeDatePO = closeDateRepository.findByUserAndDate(userPO, date);
        if (closeDatePO != null && closeDatePO.getDate().getYear() == userReport.getFromDate().getYear() &&
                closeDatePO.getDate().getMonthValue() == userReport.getFromDate().getMonthValue()) {
            userReport.setClosed(true);
        }
    }

    int calcAndUpdateProvisionHours(int workableHours, UserReport userReport) {
        int provisionHours = (int) Math.ceil((userReport.getTotalTimeProvision() - workableHours * provisionLimit));
        if (provisionHours > 0) {
            userReport.setProvisionHours(provisionHours);
            return provisionHours;
        }
        return 0;
    }
}
