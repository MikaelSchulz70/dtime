package se.dtime.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicHoliday {
    private Long id;
    private String name;
    private boolean isWorkday;
}
