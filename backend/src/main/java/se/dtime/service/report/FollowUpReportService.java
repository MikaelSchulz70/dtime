package se.dtime.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
        switch (type) {
            case USER:
                report.setFollowUpUserReports(createFollowUpUserReports(followUpData));
                break;
            case ACCOUNT:
                report.setFollowUpAccountReports(createFollowUpAccountReports(followUpData));
                break;
        }

        return report;
    }

    private BigDecimal calcTotalHours(List<FollowUpData> followUpData) {
        return followUpData.stream().
                filter(d -> d.getTotalTime() != null).
                map(FollowUpData::getTotalTime).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<FollowUpAccountReport> createFollowUpAccountReports(List<FollowUpData> followUpData) {
        List<FollowUpAccountReport> followUpAccountReports = new ArrayList<>();

        Map<Long, FollowUpAccountReport> followUpAccountReportMap = new HashMap<>();
        for (FollowUpData fd : followUpData) {
            FollowUpAccountReport followUpAccountReport = followUpAccountReportMap.get(fd.getAccountId());
            if (followUpAccountReport == null) {
                followUpAccountReport = new FollowUpAccountReport();
                followUpAccountReport.setaccountId(fd.getAccountId());
                followUpAccountReport.setAccountName(fd.getAccountName());
                followUpAccountReportMap.put(fd.getAccountId(), followUpAccountReport);
                followUpAccountReports.add(followUpAccountReport);
            }

            updateAmountAndHours(followUpAccountReport, fd);
        }

        followUpAccountReports.sort((FollowUpAccountReport r1, FollowUpAccountReport r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount()));

        return followUpAccountReports;
    }

    private List<FollowUpUserReport> createFollowUpUserReports(List<FollowUpData> followUpData) {
        List<FollowUpUserReport> followUpUserReports = new ArrayList<>();

        Map<Long, FollowUpUserReport> followUpUserReportMap = new HashMap<>();
        for (FollowUpData fd : followUpData) {
            FollowUpUserReport followUpUserReport = followUpUserReportMap.get(fd.getuserId());
            if (followUpUserReport == null) {
                followUpUserReport = new FollowUpUserReport();
                followUpUserReport.setuserId(fd.getuserId());
                followUpUserReport.setFullName(fd.getFullName());
                followUpUserReportMap.put(fd.getuserId(), followUpUserReport);
                followUpUserReports.add(followUpUserReport);
            }

            updateAmountAndHours(followUpUserReport, fd);
        }

        followUpUserReports.sort((FollowUpUserReport r1, FollowUpUserReport r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount()));
        return followUpUserReports;
    }

    private void updateAmountAndHours(FollowUpBaseReport report, FollowUpData fd) {
        BigDecimal totalHours = (fd.getTotalTime() != null ? fd.getTotalTime() : BigDecimal.ZERO);
        report.setTotalHours(report.getTotalHours().add(totalHours));
    }
}
