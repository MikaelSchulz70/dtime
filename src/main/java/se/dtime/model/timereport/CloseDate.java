package se.dtime.model.timereport;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseDate {
    private Long id;
    @NotNull(message = "Id user required")
    private Long idUser;
    @NotNull(message = "Close date required")
    private LocalDate closeDate;
}
