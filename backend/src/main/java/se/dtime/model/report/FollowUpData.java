package se.dtime.model.report;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class FollowUpData {
    private long accountId;
    private String accountName;
    private long taskId;
    private String taskName;
    private long userId;
    private String fullName;
    private BigDecimal totalTime;
    private String comment;

    public long getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public long getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public long getuserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public BigDecimal getTotalTime() {
        return totalTime;
    }

    public String getComment() {
        return comment;
    }
}
