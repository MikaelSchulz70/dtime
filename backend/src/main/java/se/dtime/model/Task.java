package se.dtime.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;
    @NotNull
    @Size(min = 1, max = 80, message = "Task name should be between 1-80 characters")
    private String name;
    @NotNull(message = "Invalid status")
    private ActivationStatus activationStatus;
    @NotNull(message = "Task type is required")
    private TaskType taskType;
    private Account account;
    private LocalDateTime createDateTime;
    private LocalDateTime updatedDateTime;
}
