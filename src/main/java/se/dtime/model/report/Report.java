package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;
import se.dtime.utils.NumberUtil;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Report {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final int workableHours;
    private List<UserReport> userReports;
    private List<ProjectReport> projectReports;
    private List<ProjectCategoryReport> projectCategoryReports;
    private List<ProjectUserReport> projectUserReports;
    private int totalWorkableHours;

    private double totalWorkedHoursExternalProvision;
    private double totalWorkedHoursInternalProvision;
    private double totalWorkedHoursExternalNoProvision;
    private double totalWorkedHoursInternalNoProvision;
    private double totalWorkedHoursOnCall;

    public Report(LocalDate fromDate, LocalDate toDate, int workableHours) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.workableHours = workableHours;
    }

    public double getTotalWorkedHours() {
        return NumberUtil.scale(totalWorkedHoursExternalProvision + totalWorkedHoursInternalProvision + totalWorkedHoursExternalNoProvision + totalWorkedHoursInternalNoProvision);
    }

    public double getTotalWorkedHoursProvision() {
        return NumberUtil.scale(totalWorkedHoursExternalProvision + totalWorkedHoursInternalProvision);
    }

    public double getTotalWorkedHoursNoProvision() {
        return NumberUtil.scale(totalWorkedHoursExternalNoProvision + totalWorkedHoursInternalNoProvision);
    }

    public double getTotalWorkedHoursPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHours(), totalWorkableHours);
    }

    public double getTotalWorkedHoursProvisionPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHoursProvision(), totalWorkableHours);
    }

    public double getTotalWorkedHoursNoProvisionPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHoursNoProvision(), totalWorkableHours);
    }

    public double getTotalWorkedHoursExternalProvisionPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHoursExternalProvision(), totalWorkableHours);
    }

    public double getTotalWorkedHoursInternalProvisionPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHoursInternalProvision(), totalWorkableHours);
    }

    public double getTotalWorkedHoursExternalNoProvisionPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHoursExternalNoProvision(), totalWorkableHours);
    }

    public double getTotalWorkedHoursInternalNoProvisionPcp() {
        return NumberUtil.divideRoundAndScalePcp(getTotalWorkedHoursInternalNoProvision(), totalWorkableHours);
    }
}
