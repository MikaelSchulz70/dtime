package se.dtime.model;

import lombok.Builder;
import se.dtime.model.timereport.Day;

@Builder
public class SessionInfo {
    private LoggedInUser loggedInUser;
    private Day currentDate;

    public LoggedInUser getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(LoggedInUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public Day getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Day currentDate) {
        this.currentDate = currentDate;
    }
}
