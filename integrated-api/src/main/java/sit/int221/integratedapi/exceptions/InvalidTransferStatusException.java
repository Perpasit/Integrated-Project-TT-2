package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransferStatusException extends RuntimeException {
    private String additionalMessage;
    public InvalidTransferStatusException(String message, String additionalMessage) {
        super(message);
        this.additionalMessage = additionalMessage;
    }
}
