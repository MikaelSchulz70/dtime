package se.dtime.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import se.dtime.model.timereport.DayType;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDay {
    private Long id;
    
    @NotNull(message = "Special day name is required")
    @Size(min = 1, max = 40, message = "Special day name must be between 1 and 40 characters")
    private String name;
    
    @NotNull(message = "Day type is required")
    private DayType dayType;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
}
