package se.dtime.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EmailPollContainer {
    private int readInLastPoll;
    private int mailInInboxInLastPoll;
    List<EmailContainer> emailContainers = new ArrayList<>();

    public void add(EmailContainer emailContainer) {
        this.emailContainers.add(emailContainer);
    }
}
