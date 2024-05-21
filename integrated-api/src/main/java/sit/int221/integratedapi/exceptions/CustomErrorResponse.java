package sit.int221.integratedapi.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.ZonedDateTime;
import java.util.List;

@JsonPropertyOrder({"timestamp", "status", "message", "instance", "errors"})
public class CustomErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;
    private int status;
    private String message;
    private String instance;
    private List<ValidationError> errors;

    public CustomErrorResponse(ZonedDateTime timestamp, int status, String message, String instance, List<ValidationError> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.instance = instance;
        this.errors = errors;
    }

    // Getters and setters...

    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        // Getters...

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }

    // Getters and setters...

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}
