package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OnCallSession {
    private Long id;
    private String lastPollDateTime;
    private long totalEmails;
    private long totalDispatched;
    private int readInLastPoll;
    private int dispatchedInLastPoll;
    private int mailInInboxInLastPoll;
    private String message;
}
