package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OnCallProject {
    private long idProject;
    private String projectName;
    private String companyName;
    private List<OnCallUser> onCallUsers;
}
