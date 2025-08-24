package se.dtime.model.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UnclosedUserReport {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<UnclosedUser> unclosedUsers;
    private int workableHours;
}