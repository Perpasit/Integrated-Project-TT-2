package sit.int221.integratedapi.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;
import java.util.List;

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

    public static class ValidationError {
        private String field;
        private String message;
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
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

}
