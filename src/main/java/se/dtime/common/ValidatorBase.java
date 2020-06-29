package se.dtime.common;

import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.model.UserExt;
import se.dtime.model.error.InvalidInputException;
import se.dtime.model.error.NotAuthorizedException;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;

public abstract class ValidatorBase<T> {

    public abstract void validateAdd(T entity);
    public abstract void validateDelete(long idEntity);
    public abstract void validateUpdate(T entity);

    public void validateLoggedIn() {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userExt == null) {
            throw new NotAuthorizedException("user.not.logged.in");
        }
    }

    protected void check(boolean isOk, String messageKey) {
        if (!isOk) {
            throw new ValidationException(messageKey);
        }
    }

    protected void checkInvalidInput(boolean isValid, String errorMsgKey) {
        if (!isValid) {
            throw new InvalidInputException(errorMsgKey);
        }
    }

    protected void checkNotFound(Object obj, String messageKey) {
        if (obj == null) {
            throw new NotFoundException(messageKey);
        }
    }
}
