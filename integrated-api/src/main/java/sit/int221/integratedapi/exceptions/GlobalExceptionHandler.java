package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.ZonedDateTime;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ValidationErrorException.class)
    public ResponseEntity<Object> handleValidationErrorException(ValidationErrorException ex, WebRequest request) {
        List<CustomErrorResponse.ValidationError> errors = new ArrayList<>();
        for (Map.Entry<String, String> entry : ex.getErrors().entrySet()) {
            errors.add(new CustomErrorResponse.ValidationError(entry.getKey(), entry.getValue()));
        }
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getReason(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Object> handleItemNotFoundException(ItemNotFoundException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateStatusException.class)
    public ResponseEntity<Object> handleDuplicateStatusException(DuplicateStatusException ex, WebRequest request) {
        List<CustomErrorResponse.ValidationError> errors = new ArrayList<>();
        errors.add(new CustomErrorResponse.ValidationError("name", "must be unique"));
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotAllowedOperationException.class)
    public ResponseEntity<Object> handleNotAllowedOperationException(NotAllowedOperationException ex, WebRequest request) {
        List<CustomErrorResponse.ValidationError> errors = new ArrayList<>();
        errors.add(new CustomErrorResponse.ValidationError("name", ex.getMessage()));

        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransferTasksRequiredException.class)
    public ResponseEntity<Object> handleTransferTasksRequiredException(TransferTasksRequiredException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransferStatusException.class)
    public ResponseEntity<CustomErrorResponse> handleInvalidTransferStatusException(InvalidTransferStatusException ex, WebRequest request) {
        List<CustomErrorResponse.ValidationError> errors = new ArrayList<>();
        errors.add(new CustomErrorResponse.ValidationError("message", "the specified status for task transfer does not exist"));

        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
