package se.dtime.model;

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
    private String name;
    private DayType dayType;
    private LocalDate date;
}
