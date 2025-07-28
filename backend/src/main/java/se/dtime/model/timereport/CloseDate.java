package se.dtime.model.timereport;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseDate {
    private Long id;
    @NotNull(message = "Id user required")
    private Long userId;
    @NotNull(message = "Close date required")
    private LocalDate closeDate;
}
