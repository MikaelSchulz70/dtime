package se.dtime.model.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Builder
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    public ApiError(HttpStatus httpStatus, String message) {
        this.timestamp = Instant.now();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = message;
    }

    public ApiError(HttpStatus httpStatus, String message, String path) {
        this.timestamp = Instant.now();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    public ApiError(FieldError fieldError) {
        this.timestamp = Instant.now();
        this.status = HttpStatus.BAD_REQUEST.value();
        this.error = HttpStatus.BAD_REQUEST.getReasonPhrase();
        this.message = "Validation failed";
        this.fieldErrors = Arrays.asList(fieldError);
    }

    public ApiError(List<FieldError> fieldErrors) {
        this.timestamp = Instant.now();
        this.status = HttpStatus.BAD_REQUEST.value();
        this.error = HttpStatus.BAD_REQUEST.getReasonPhrase();
        this.message = "Validation failed";
        this.fieldErrors = fieldErrors;
    }

    // All args constructor for Builder
    public ApiError(Instant timestamp, int status, String error, String message, String path, List<FieldError> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    // Support legacy getStatus() method
    public HttpStatus getStatus() {
        return HttpStatus.valueOf(this.status);
    }
}
