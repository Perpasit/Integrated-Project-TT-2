package sit.int221.integratedapi.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.WebRequest;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler  {

    @ExceptionHandler(ValidationErrorException.class)
    public ResponseEntity<Object> handleValidationErrorException(ValidationErrorException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getReason(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        for (Map.Entry<String, String> entry : ex.getErrors().entrySet()) {
            errorResponse.addValidationError(entry.getKey(), entry.getValue());
        }

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
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );

        errorResponse.addValidationError("name", "must be unique");

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
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );

        errorResponse.addValidationError("name", ex.getMessage());

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
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );

        errorResponse.addValidationError("message", "the specified status for task transfer does not exist");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation error. Check 'errors' field for details.",
                request.getDescription(false),
                null
        );

        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(StatusLimitReachedException.class)
    public ResponseEntity<Object> handleStatusLimitReachedException(StatusLimitReachedException ex, WebRequest request) {
        List<CustomErrorResponse.ValidationError> errors = new ArrayList<>();
        errors.add(new CustomErrorResponse.ValidationError("status", ex.getMessage()));

        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CustomErrorResponse> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token has expired",
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<CustomErrorResponse> handleMalformedJwtException(MalformedJwtException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token is malformed",
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<CustomErrorResponse> handleSignatureException(SignatureException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ZonedDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token signature is invalid",
                request.getDescription(false).replace("uri=", ""),
                Collections.emptyList()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }



}


