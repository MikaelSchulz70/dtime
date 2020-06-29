package se.dtime.model.timereport;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class Vacations {
    private Day[] days;
    private List<UserVacation> userVacations;

    public LocalDate getFirstDate() {
        return days.length > 0 ? days[0].getDate() : null;
    }

    public LocalDate getLastDate() {
        return days.length > 0 ? days[days.length - 1].getDate() : null;
    }
}
