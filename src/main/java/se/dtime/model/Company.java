package se.dtime.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    private Long id;

    @NotNull
    @Size(min=1, max=40, message="Company name should have between 1-40 characters")
    private String name;

    @NotNull(message = "Invalid status")
    private ActivationStatus activationStatus;

    private LocalDateTime createDateTime;
    private LocalDateTime updatedDateTime;
}
