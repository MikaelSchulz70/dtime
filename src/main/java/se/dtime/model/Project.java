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
public class Project {
    private Long id;
    @NotNull
    @Size(min = 1, max = 80, message = "Project name should have between 1-80 characters")
    private String name;
    @NotNull(message = "Invalid status")
    private ActivationStatus activationStatus;
    @NotNull(message = "Invalid project category")
    private ProjectCategory projectCategory;
    private Company company;
    private boolean provision;
    private boolean internal;
    private boolean onCall;
    private boolean fixRate;
    private LocalDateTime createDateTime;
    private LocalDateTime updatedDateTime;
}
