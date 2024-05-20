package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomErrorResponse extends RuntimeException {
    public CustomErrorResponse(String message) {
        super(message);
    }
}