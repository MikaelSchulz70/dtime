package se.dtime.model.report;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReport {
    private Long accountId;
    private String accountName;
    private BigDecimal totalHours;
}
