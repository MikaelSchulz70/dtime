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
        return new StringBuilder()
                .append("To: ").append(toList).append("\n")
                .append("From: ").append(from).append("\n")
                .append("CC: ").append(ccList).append("\n")
                .append("Subject: ").append(subject).append("\n")
                .append("Sent Date: ").append(sentDate).append("\n")
                .append("Message: ").append(body).append("\n")
                .toString();
    }
}
