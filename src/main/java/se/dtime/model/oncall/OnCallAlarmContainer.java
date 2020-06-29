package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OnCallAlarmContainer {
    List<OnCallAlarm> onCallAlarms;
    boolean isAdmin;
}
