package se.dtime.model.error;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private String messageKey;

    public BaseException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }
}
