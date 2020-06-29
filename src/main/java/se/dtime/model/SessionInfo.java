package se.dtime.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.dtime.model.timereport.Day;

@Getter
@Setter
@Builder
public class SessionInfo {
    private LoggedInUser loggedInUser;
    private Day currentDate;
    private boolean showOnCall;
}
