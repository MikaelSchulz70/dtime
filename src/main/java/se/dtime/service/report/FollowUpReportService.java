package se.dtime.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.model.ProjectCategory;
import se.dtime.model.ReportDates;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.report.*;
import se.dtime.repository.jdbc.FollowUpReportRepository;
import se.dtime.service.calendar.CalendarService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FollowUpReportService {

    @Autowired
    private CalendarService calendarService;
    @Autowired
    private FollowUpReportRepository followUpReportRepository;

    public FollowUpReport getCurrentReport(ReportView reportView, FollowUpReportType type) {
        return getReport(reportView, type, calendarService.getNowDate());
    }

    public FollowUpReport getPreviousReport(ReportView reportView, FollowUpReportType type, LocalDate date) {
        LocalDate reportDate = ReportUtil.getPreviousDate(reportView, date);
        return getReport(reportView, type, reportDate);
    }

    public FollowUpReport getNextReport(ReportView reportView, FollowUpReportType type, LocalDate date) {
        LocalDate reportDate = ReportUtil.getNextDate(reportView, date);
        return getReport(reportView, type, reportDate);
    }

    private FollowUpReport getReport(ReportView reportView, FollowUpReportType type, LocalDate date) {
        ReportDates reportDates = ReportUtil.getReportDates(reportView, date);
        return getReportBetweenDates(reportDates, type);
    }

    private FollowUpReport getReportBetweenDates(ReportDates reportDates, FollowUpReportType type) {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userExt == null) {
            throw new NotFoundException("user.not.logged.in");
        }


        FollowUpReport report = new FollowUpReport(reportDates.getFromDate(), reportDates.getToDate());

        List<FollowUpData> followUpData = followUpReportRepository.getFollowUpData(reportDates.getFromDate(), reportDates.getToDate());
        report.setTotalHours(calcTotalHours(followUpData));
        report.setAmount(calcAmount(followUpData));
        report.setAmountSubcontractor(calcTotalSubcontractorAmount(followUpData));
        report.setTotalAmount(calcTotalAmount(followUpData));
        switch (type) {
            case USER:
                report.setFollowUpUserReports(createFollowUpUserReports(followUpData));
                break;
            case COMPANY:
                report.setFollowUpCompanyReports(createFollowUpCompanyReports(followUpData));
                break;
            case PROJECT_CATEGORY:
                report.setFollowUpCategoryReports(createFollowUpCategoryReports(followUpData));
                break;
        }

        return report;
    }

    private BigDecimal calcAmount(List<FollowUpData> followUpData) {
        return followUpData.stream().
                filter(d -> d.getAmount() != null).
                map(d -> d.getAmountSubcontractor() != null ? d.getAmount().subtract(d.getAmountSubcontractor()) : d.getAmount()).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcTotalSubcontractorAmount(List<FollowUpData> followUpData) {
        return followUpData.stream().
                filter(d -> d.getAmountSubcontractor() != null).
                map(FollowUpData::getAmountSubcontractor).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcTotalAmount(List<FollowUpData> followUpData) {
        return followUpData.stream().
                filter(d -> d.getAmount() != null).
                map(FollowUpData::getAmount).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcTotalHours(List<FollowUpData> followUpData) {
        return followUpData.stream().
                filter(d -> d.getTotalTime() != null).
                map(FollowUpData::getTotalTime).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<FollowUpCategoryReport> createFollowUpCategoryReports(List<FollowUpData> followUpData) {
        List<FollowUpCategoryReport> followUpCategoryReports = new ArrayList<>();

        Map<ProjectCategory, FollowUpCategoryReport> followUpCategoryReportMap = new HashMap<>();
        for (FollowUpData fd : followUpData) {
            FollowUpCategoryReport followUpCategoryReport = followUpCategoryReportMap.get(fd.getCategory());
            if (followUpCategoryReport == null) {
                followUpCategoryReport = new FollowUpCategoryReport();
                followUpCategoryReport.setProjectCategory(fd.getCategory());

                followUpCategoryReportMap.put(fd.getCategory(), followUpCategoryReport);
                followUpCategoryReports.add(followUpCategoryReport);

                FollowUpUserReport followUpUserReport = new FollowUpUserReport();
                followUpUserReport.setIdUser(fd.getIdUser());
                followUpUserReport.setUserName(fd.getUserName());

                List<FollowUpUserReport> followUpUserReports = new ArrayList<>();
                followUpUserReports.add(followUpUserReport);

                followUpCategoryReport.setFollowUpUserReports(followUpUserReports);
            }

            updateAmountAndHours(followUpCategoryReport, fd);

            FollowUpUserReport followUpUserReport = followUpCategoryReport.getFollowUpUserReport(fd.getIdUser());
            if (followUpUserReport == null) {
                followUpUserReport = new FollowUpUserReport();
                followUpUserReport.setIdUser(fd.getIdUser());
                followUpUserReport.setUserName(fd.getUserName());
                followUpCategoryReport.getFollowUpUserReports().add(followUpUserReport);
            }

            updateAmountAndHours(followUpUserReport, fd);
        }

        followUpCategoryReports.sort((FollowUpCategoryReport r1, FollowUpCategoryReport r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount()));
        followUpCategoryReports.forEach(r -> r.getFollowUpUserReports().sort((FollowUpUserReport r1, FollowUpUserReport r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount())));

        return followUpCategoryReports;
    }

    private List<FollowUpCompanyReport> createFollowUpCompanyReports(List<FollowUpData> followUpData) {
        List<FollowUpCompanyReport> followUpCompanyReports = new ArrayList<>();

        Map<Long, FollowUpCompanyReport> followUpCompanyReportMap = new HashMap<>();
        for (FollowUpData fd : followUpData) {
            FollowUpCompanyReport followUpCompanyReport = followUpCompanyReportMap.get(fd.getIdCompany());
            if (followUpCompanyReport == null) {
                followUpCompanyReport = new FollowUpCompanyReport();
                followUpCompanyReport.setIdCompany(fd.getIdCompany());
                followUpCompanyReport.setCompanyName(fd.getCompanyName());
                followUpCompanyReportMap.put(fd.getIdCompany(), followUpCompanyReport);
                followUpCompanyReports.add(followUpCompanyReport);
            }

            updateAmountAndHours(followUpCompanyReport, fd);
        }

        followUpCompanyReports.sort((FollowUpCompanyReport r1, FollowUpCompanyReport r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount()));

        return followUpCompanyReports;
    }

    private List<FollowUpUserReport> createFollowUpUserReports(List<FollowUpData> followUpData) {
        List<FollowUpUserReport> followUpUserReports = new ArrayList<>();

        Map<Long, FollowUpUserReport> followUpUserReportMap = new HashMap<>();
        for (FollowUpData fd : followUpData) {
            FollowUpUserReport followUpUserReport = followUpUserReportMap.get(fd.getIdUser());
            if (followUpUserReport == null) {
                followUpUserReport = new FollowUpUserReport();
                followUpUserReport.setIdUser(fd.getIdUser());
                followUpUserReport.setUserName(fd.getUserName());
                followUpUserReportMap.put(fd.getIdUser(), followUpUserReport);
                followUpUserReports.add(followUpUserReport);
            }

            updateAmountAndHours(followUpUserReport, fd);
        }

        followUpUserReports.sort((FollowUpUserReport r1, FollowUpUserReport r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount()));
        return followUpUserReports;
    }

    private void updateAmountAndHours(FollowUpBaseReport report, FollowUpData fd) {
        BigDecimal totalHours = (fd.getTotalTime() != null ? fd.getTotalTime() : BigDecimal.ZERO);
        BigDecimal amountTotal = (fd.getAmount() != null ? fd.getAmount() : BigDecimal.ZERO);
        BigDecimal amountSubcontractor = (fd.getAmountSubcontractor() != null ? fd.getAmountSubcontractor() : BigDecimal.ZERO);
        BigDecimal amount = amountTotal.subtract(amountSubcontractor);

        report.setTotalHours(report.getTotalHours().add(totalHours));
        report.setAmount(report.getAmount().add(amount));
        report.setAmountSubcontractor(report.getAmountSubcontractor().add(amountSubcontractor));
        report.setTotalAmount(report.getTotalAmount().add(amountTotal));

        if (fd.getAmount() == null) {
            report.setComment("Missing amount for " + fd.getTotalTime() + " hours");
        }
    }

}
