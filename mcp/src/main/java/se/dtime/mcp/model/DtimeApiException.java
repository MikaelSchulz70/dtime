package se.dtime.mcp.model;

/**
 * Exception thrown when API communication fails.
 */
public class DtimeApiException extends RuntimeException {
    public DtimeApiException(String message) {
        super(message);
    }

    public DtimeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

