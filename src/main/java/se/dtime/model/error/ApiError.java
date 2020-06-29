package se.dtime.model.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class ApiError {
    private HttpStatus status;
    private String error;
    private List<FieldError> fieldErrors;

    public ApiError(HttpStatus status, String error) {
        this.status = status;
        this.error = error;
    }

    public ApiError(FieldError fieldError) {
        this.status = HttpStatus.BAD_REQUEST;
        this.fieldErrors = Arrays.asList(fieldError);
    }

    public ApiError(List<FieldError> fieldErrors) {
        this.status = HttpStatus.BAD_REQUEST;
        this.fieldErrors = fieldErrors;
    }
}
