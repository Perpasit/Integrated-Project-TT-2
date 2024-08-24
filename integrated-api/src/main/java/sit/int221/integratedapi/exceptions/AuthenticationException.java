package sit.int221.integratedapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthenticationException {
    public static ResponseEntity<Object> getMessage(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", ZonedDateTime.now().toString());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("message", message);
        body.put("errors", Collections.emptyList());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}