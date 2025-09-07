package se.dtime.model.error;

import lombok.Getter;

@Getter
public class DTimeException extends RuntimeException {
    private final String messageKey;

    public DTimeException(String messageKey, Exception e) {
        super(messageKey, e);
        this.messageKey = messageKey;
    }
}
