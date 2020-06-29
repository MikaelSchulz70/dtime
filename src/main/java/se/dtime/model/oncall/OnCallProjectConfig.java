package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OnCallProjectConfig {
    private long idProject;
    private String companyName;
    private String projectName;
    private List<OnCallDayConfig> onCallDayConfigs;
}
