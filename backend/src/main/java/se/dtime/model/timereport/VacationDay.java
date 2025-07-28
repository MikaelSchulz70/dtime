package se.dtime.model.timereport;

import lombok.*;

import jakarta.validation.constraints.NotNull;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationDay {
    @NotNull
    private Day day;
    private boolean isVacation = false;
}
