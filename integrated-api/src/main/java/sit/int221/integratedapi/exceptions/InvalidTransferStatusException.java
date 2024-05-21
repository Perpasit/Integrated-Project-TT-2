package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransferStatusException extends RuntimeException {
    public InvalidTransferStatusException(String message) {
        super(message);
    }
}