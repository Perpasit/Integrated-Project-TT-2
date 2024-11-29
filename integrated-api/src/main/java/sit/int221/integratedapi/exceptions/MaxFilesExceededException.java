package sit.int221.integratedapi.exceptions;

public class MaxFilesExceededException extends RuntimeException{

    public MaxFilesExceededException(String message) {
        super(message);
    }
}
