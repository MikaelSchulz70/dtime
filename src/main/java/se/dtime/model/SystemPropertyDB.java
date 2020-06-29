package se.dtime.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemPropertyDB {
    private Long id;
    @NotNull
    @Size(min=1, max=80, message="Name max 50 characters")
    private String name;
    @Size(min=0, max=100, message="Value max 100 characters")
    private String value;
    private SystemPropertyType systemPropertyType;
    private String description;
}
