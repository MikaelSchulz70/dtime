package se.dtime.model.oncall;

import lombok.*;
import se.dtime.model.Project;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnCallRule {
    private Long id;
    private Project project;
    @NotNull(message = "Mandatory. E.g. info@dtime.se, @dtime.se")
    @Size(min = 4, max = 60, message = "Length between  4-60 characters")
    private String fromMail;
    @Size(min = 0, max = 100, message = "Length between 0-100 characters")
    private String subjectCSV;
    @Size(min = 0, max = 100, message = "Length between 0-100 characters")
    private String bodyCSV;
}
