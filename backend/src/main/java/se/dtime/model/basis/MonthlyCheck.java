package se.dtime.model.basis;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class MonthlyCheck {
    private Long id;
    private long accountId;

    @NotNull(message = "Date not specified")
    private LocalDate date;
}
