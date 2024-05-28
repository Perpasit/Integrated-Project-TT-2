package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFieldException extends RuntimeException {
    private final String timestamp;
    private final int status;
    private final String message;
    private final String instance;
    private final List<Map<String, String>> errors;

    public InvalidFieldException(String field, String message, String instance) {
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.status = HttpStatus.BAD_REQUEST.value();
        this.message = field + " must not be null";
        this.instance = instance;
        this.errors = List.of(Map.of("field", field, "message", "must not be null"));
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getInstance() {
        return instance;
    }

    public List<Map<String, String>> getErrors() {
        return errors;
    }
}