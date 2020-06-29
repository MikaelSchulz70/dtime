package se.dtime.model.basis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class MonthlyCheck {
    private Long id;
    private long companyId;

    @NotNull(message = "Date not specified")
    private LocalDate date;
    private boolean invoiceVerified;
    private boolean invoiceSent;
}
