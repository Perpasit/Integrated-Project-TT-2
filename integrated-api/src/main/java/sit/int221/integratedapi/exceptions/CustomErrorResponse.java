package sit.int221.integratedapi.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        private String fieldName; // เปลี่ยนชื่อ field เป็น fieldName
        private String message;

        public ValidationError(String fieldName, String message) {
            this.fieldName = fieldName; // เปลี่ยนชื่อ field เป็น fieldName
            this.message = message;
        }

        public String getFieldName() { // เปลี่ยนชื่อเมท็อดเป็น getFieldName
            return fieldName; // เปลี่ยนชื่อ field เป็น fieldName
        }

        public void setFieldName(String fieldName) { // เพิ่มเมท็อด setFieldName
            this.fieldName = fieldName; // เปลี่ยนชื่อ field เป็น fieldName
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

    public void addValidationError(String field, String message) {
        if (Objects.isNull(errors)) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}
