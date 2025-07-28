package se.dtime.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskContributor {
    private Long id;
    @NotNull
    private User user;
    @NotNull
    private Task task;
    @NotNull
    private ActivationStatus activationStatus;
}
