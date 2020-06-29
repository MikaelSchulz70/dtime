package se.dtime.dbmodel.oncall;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity(name = "OnCallSession")
@Table(name = "oncallsession")
public class OnCallSessionPO {
    private Long id;
    private LocalDateTime lastPollDateTime;
    private long totalEmails;
    private long totalDispatched;
    private int readInLastPoll;
    private int dispatchedInLastPoll;
    private int mailInInboxInLastPoll;
    private String message;

    public OnCallSessionPO() {

    }

    public OnCallSessionPO(long id, LocalDateTime lastPollDateTime) {
        this.id = id;
        this.lastPollDateTime = lastPollDateTime;
    }

    public OnCallSessionPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "datetime", unique = true, nullable = false)
    public LocalDateTime getLastPollDateTime() {
        return lastPollDateTime;
    }

    public void setLastPollDateTime(LocalDateTime lastPollDateTime) {
        this.lastPollDateTime = lastPollDateTime;
    }

    @Column(name = "totalemails")
    public long getTotalEmails() {
        return totalEmails;
    }

    public void setTotalEmails(long totalEmails) {
        this.totalEmails = totalEmails;
    }

    @Column(name = "totaldispatched")
    public long getTotalDispatched() {
        return totalDispatched;
    }

    public void setTotalDispatched(long totalDispatched) {
        this.totalDispatched = totalDispatched;
    }

    @Column(name = "readinlastpoll")
    public int getReadInLastPoll() {
        return readInLastPoll;
    }

    public void setReadInLastPoll(int readInLastPoll) {
        this.readInLastPoll = readInLastPoll;
    }

    @Column(name = "dispatchedinlastpoll")
    public int getDispatchedInLastPoll() {
        return dispatchedInLastPoll;
    }

    public void setDispatchedInLastPoll(int dispatchedInLastPoll) {
        this.dispatchedInLastPoll = dispatchedInLastPoll;
    }

    @Column(name = "mailininboxinlastpoll")
    public int getMailInInboxInLastPoll() {
        return mailInInboxInLastPoll;
    }

    public void setMailInInboxInLastPoll(int mailInInboxInLastPoll) {
        this.mailInInboxInLastPoll = mailInInboxInLastPoll;
    }

    @Column(name = "message", unique = true, nullable = false, length = 250)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
