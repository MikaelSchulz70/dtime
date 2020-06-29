package se.dtime.model.oncall;

import lombok.*;
import se.dtime.model.timereport.Day;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnCallDay {
    private long id;
    private long idAssignment;
    private boolean isOnCall;
    @NotNull
    private Day day;
    private boolean isReadOnly;
}
