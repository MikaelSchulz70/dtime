package se.dtime.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class EmailContainer {
    private String from;
    private String subject;
    private String body;
    private String toList;
    private String ccList;
    private String sentDate;

    @Override
    public String toString() {
        return "To: " + toList + "\n" +
                "From: " + from + "\n" +
                "CC: " + ccList + "\n" +
                "Subject: " + subject + "\n" +
                "Sent Date: " + sentDate + "\n" +
                "Message: " + body + "\n";
    }
}
