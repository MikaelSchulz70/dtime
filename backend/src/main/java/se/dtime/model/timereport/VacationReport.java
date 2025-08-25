package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class VacationReport {
    private String firstDate;
    private String lastDate;
    private Day[] days;
    private List<UserVacation> userVacations;
}