package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;
    private final List<CustomErrorResponse.ValidationError> errors;

    public ValidationErrorException(HttpStatus status, String reason) {
        super(reason);
        this.status = status;
        this.reason = reason;
        this.errors = new ArrayList<>();
    }

    public ValidationErrorException(HttpStatus status, String reason, List<CustomErrorResponse.ValidationError> errors) {
        super(reason);
        this.status = status;
        this.reason = reason;
        this.errors = errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<CustomErrorResponse.ValidationError> getErrors() {
        return errors;
    }

    public void addError(String field, String message) {
        this.errors.add(new CustomErrorResponse.ValidationError(field, message));
    }
}
