package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FollowUpBaseReport {
    private BigDecimal totalHours = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal amountSubcontractor = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String comment;
}
