package se.dtime.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    private Long id;
    @NotNull
    private User user;
    @NotNull
    private Project project;
    @NotNull
    private ActivationStatus activationStatus;
}
