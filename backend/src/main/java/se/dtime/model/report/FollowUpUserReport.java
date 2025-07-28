package se.dtime.model.report;

public class FollowUpUserReport extends FollowUpBaseReport {
    private long userId;
    private String fullName;

    public long getuserId() {
        return userId;
    }

    public void setuserId(long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
