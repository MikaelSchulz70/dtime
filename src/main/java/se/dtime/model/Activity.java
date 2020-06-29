package se.dtime.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private Long id;
    @NotNull
    private String description;
    private String addedBy;
    private boolean voted;
    private int noOfVotes;
}