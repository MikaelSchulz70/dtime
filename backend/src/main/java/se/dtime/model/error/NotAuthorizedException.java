package se.dtime.model.error;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotAuthorizedException extends BaseException {
    public NotAuthorizedException(String messageKey) {
        super(messageKey);
    }
}