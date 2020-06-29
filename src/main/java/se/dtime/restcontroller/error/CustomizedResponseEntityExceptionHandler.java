package se.dtime.restcontroller.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import se.dtime.common.Messages;
import se.dtime.model.error.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler {
    @Autowired
    private DataIntegrityViolationHandler dataIntegrityViolationHandler;
    @Autowired
    private Messages messages;

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiError> handleAllExceptions(Exception ex, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        log.error("Sever error", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ApiError> handleAccessDeniedException(Exception ex, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.UNAUTHORIZED, ex.getMessage());
        log.error("Sever error", ex);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public final ResponseEntity<ApiError> handleDataIntegrityViolationException(Exception ex, WebRequest request) {
        ApiError apiError;
        FieldError fieldError = dataIntegrityViolationHandler.getFieldError(ex.getMessage());
        if (fieldError != null) {
            apiError = new ApiError(fieldError);
        } else {
            apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public final ResponseEntity<ApiError> handleNotAuthorizedException(NotAuthorizedException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, messages.get(ex.getMessageKey()));
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(ValidationException.class)
    public final ResponseEntity<ApiError> handleValidationException(ValidationException ex, WebRequest request) {
        ApiError apiError;
        if (ex.isFieldName()) {
            FieldError fieldError = new FieldError(ex.getFieldName(), messages.get(ex.getMessageKey()));
            apiError = new ApiError(fieldError);
        } else {
            apiError = new ApiError(HttpStatus.BAD_REQUEST, messages.get(ex.getMessageKey()));
        }

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(InvalidInputException.class)
    public final ResponseEntity<ApiError> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, messages.get(ex.getMessageKey()));
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ApiError> handleUserNotFoundException(NotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, messages.get(ex.getMessageKey()));
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();

        List<FieldError> fieldErrors = result.getFieldErrors().stream().
                map(e -> new FieldError(e.getField(), e.getDefaultMessage())).collect(Collectors.toList());

        ApiError apiError = new ApiError(fieldErrors);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
