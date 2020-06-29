package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserVacation {
    long idUser;
    private String name;
    private int noVacationDays;
    private VacationDay[] vacationsDays;
}
