package se.dtime.model.report;

import lombok.*;
import se.dtime.model.UserCategory;
import se.dtime.utils.NumberUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReport {
    private long idUser;
    private UserCategory userCategory;
    private String userName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private double totalTimeInternalProvision = 0;
    private double totalTimeInternalNoProvision = 0;
    private double totalTimeExternalProvision = 0;
    private double totalTimeExternalNoProvision = 0;
    private double totalTimeOnCall = 0;
    private int provisionHours = 0;
    private boolean isClosed;
    private List<ProjectReport> projectReports = new ArrayList<>();

    public double getTotalTime() {
        return NumberUtil.scale(totalTimeInternalProvision + totalTimeInternalNoProvision + totalTimeExternalProvision + totalTimeExternalNoProvision);
    }

    public double getTotalTimeProvision() {
        return NumberUtil.scale(totalTimeInternalProvision + totalTimeExternalProvision);
    }

    public double getTotalTimeNoProvision() {
        return NumberUtil.scale(totalTimeInternalNoProvision + totalTimeExternalNoProvision);
    }
}
