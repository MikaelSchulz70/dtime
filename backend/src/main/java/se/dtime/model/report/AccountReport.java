package se.dtime.model.report;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReport {
    private Long accountId;
    private String accountName;
    private double totalHours;
}
