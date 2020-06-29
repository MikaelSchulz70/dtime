package se.dtime.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    private long id;
    private String name;
    private String value;
}
