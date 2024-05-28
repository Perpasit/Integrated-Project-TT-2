package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

public class ValidationErrorException extends RuntimeException {
    private HttpStatus status;
    private String reason;
    private Map<String, String> errors;

    public ValidationErrorException(HttpStatus status, String reason) {
        this.status = status;
        this.reason = reason;
        this.errors = new HashMap<>();
    }

    public void addError(String field, String message) {
        this.errors.put(field, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
