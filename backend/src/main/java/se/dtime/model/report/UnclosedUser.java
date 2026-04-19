package se.dtime.model.report;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UnclosedUser {
    private Long userId;
    private String fullName;
    private String email;
    private BigDecimal totalTime;
    private boolean closed;
}