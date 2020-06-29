package se.dtime.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailContainer {
    private String from;
    private String subject;
    private String body;
    private String toList;
    private String ccList;
    private String sentDate;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("To: ").append(toList).append("\n");
        sb.append("From: ").append(from).append("\n");
        sb.append("CC: ").append(ccList).append("\n");
        sb.append("Subject: ").append(subject).append("\n");
        sb.append("Sent Date: ").append(sentDate).append("\n");
        sb.append("Message: ").append(body).append("\n");
        return sb.toString();
    }
}
