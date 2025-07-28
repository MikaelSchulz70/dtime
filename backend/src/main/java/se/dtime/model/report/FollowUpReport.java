package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FollowUpReport {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalHours;
    private List<FollowUpUserReport> followUpUserReports;
    private List<FollowUpAccountReport> followUpAccountReports;
    private List<FollowUpTaskyReport> followUpTaskyReports;

    public FollowUpReport(LocalDate fromDate, LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
}
