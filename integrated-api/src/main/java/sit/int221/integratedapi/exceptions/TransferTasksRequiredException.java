package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TransferTasksRequiredException extends RuntimeException {
    public TransferTasksRequiredException(String message) {
        super(message);
    }
}

