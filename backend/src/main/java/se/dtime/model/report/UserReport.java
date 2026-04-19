package se.dtime.model.report;

import lombok.*;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReport {
    private long userId;
    private String fullName;
    private String email;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalTime = BigDecimal.ZERO;
    private boolean isClosed;
    private List<TaskReport> taskReports = new ArrayList<>();
}
