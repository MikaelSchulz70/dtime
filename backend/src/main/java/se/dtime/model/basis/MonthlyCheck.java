package se.dtime.model.basis;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class MonthlyCheck {
    private Long id;
    
    @Positive(message = "Account ID must be positive")
    private long accountId;

    @NotNull(message = "Date not specified")
    private LocalDate date;
}
