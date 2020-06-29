package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OnCallConfig {
    private boolean readOnly;
    private String onCallPhoneNumber;
    private List<OnCallProjectConfig> onCallProjectConfigs;
}
