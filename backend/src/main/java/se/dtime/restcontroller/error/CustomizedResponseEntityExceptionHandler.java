package se.dtime.restcontroller.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import se.dtime.common.Messages;
import se.dtime.model.error.*;

import java.util.List;

@Slf4j
@ControllerAdvice
public class CustomizedResponseEntityExceptionHandler {

    private final DataIntegrityViolationHandler dataIntegrityViolationHandler;
    private final Messages messages;

    // Constructor injection instead of @Autowired
    public CustomizedResponseEntityExceptionHandler(
            DataIntegrityViolationHandler dataIntegrityViolationHandler,
            Messages messages) {
        this.dataIntegrityViolationHandler = dataIntegrityViolationHandler;
        this.messages = messages;
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiError> handleAllExceptions(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiError apiError = ApiError.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An internal server error occurred")
                .path(path)
                .build();

        log.error("Server error at path {}: {}", path, ex.getMessage(), ex);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiError apiError = ApiError.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.FORBIDDEN.value()) // Should be 403, not 401
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access denied")
                .path(path)
                .build();

        log.warn("Access denied at path {}: {}", path, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiError apiError = ApiError.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid parameter type")
                .message("Parameter '" + ex.getName() + "' has invalid type: " + ex.getMessage())
                .path(path)
                .build();
        
        log.warn("Invalid parameter type at path {}: {}", path, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiError apiError = ApiError.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing required parameter")
                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                .path(path)
                .build();
        
        log.warn("Missing required parameter at path {}: {}", path, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiError apiError = ApiError.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid argument")
                .message(ex.getMessage())
                .path(path)
                .build();
        
        log.warn("Invalid argument at path {}: {}", path, ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiError apiError = ApiError.builder()
                .timestamp(java.time.Instant.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .message(ex.getMessage())
                .path(path)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(InvalidInputException.class)
    public final ResponseEntity<ApiError> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, messages.get(ex.getMessageKey()));
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
                map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();

        ApiError apiError = new ApiError(fieldErrors);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
