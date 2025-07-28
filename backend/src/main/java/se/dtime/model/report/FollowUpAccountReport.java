package se.dtime.model.report;

public class FollowUpAccountReport extends FollowUpBaseReport {
    private long accountId;
    private String accountName;

    public long getaccountId() {
        return accountId;
    }

    public void setaccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
