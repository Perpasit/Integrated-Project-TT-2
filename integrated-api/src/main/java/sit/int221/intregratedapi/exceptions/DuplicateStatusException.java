package sit.int221.intregratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class DuplicateStatusException extends RuntimeException {
    public DuplicateStatusException(String message) {
        super(message);
    }
}