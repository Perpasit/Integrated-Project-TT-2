package sit.int221.integratedapi.exceptions;

public class AuthenticationException extends RuntimeException {

    private final String message;

    public AuthenticationException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
