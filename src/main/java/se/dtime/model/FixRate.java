package se.dtime.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixRate {
    private long id;
    private long idProject;
    private String companyName;
    private String projectName;
    private BigDecimal rate;
    @NotNull(message = "From date required")
    private LocalDate fromDate;
    private LocalDate toDate;
    @Size(min = 0, max = 250, message = "Max 250 characters")
    private String comment;

}
