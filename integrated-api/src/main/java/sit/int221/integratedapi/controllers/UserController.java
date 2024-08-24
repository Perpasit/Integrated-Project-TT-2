package sit.int221.integratedapi.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.integratedapi.dtos.JwtRequestUser;
import sit.int221.integratedapi.dtos.UserDTO;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.AuthenticationException;
import sit.int221.integratedapi.services.UserService;
import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.services.JwtUserDetailsService;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody JwtRequestUser jwtRequestUser) {
        try {
            // Check if the username exists first
            User user = userService.getUserByUsername(jwtRequestUser.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("The username or password is incorrect.");
            }
            // Now check the password
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(jwtRequestUser.getUsername(), jwtRequestUser.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The username or password is incorrect.");
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a problem. Please try again later.");
        }
    }





    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String requestTokenHeader) {
        Claims claims = null;
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            } catch (ExpiredJwtException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "JWT Token does not begin with Bearer String");
        }
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{username}")
    public ResponseEntity<Object> getUserByUsername(@PathVariable("username") String username) {
        try {
            String name = userService.getNameByUsername(username);
            if (name != null) {
                logger.info("User fetched successfully for username: {}", username);
                return ResponseEntity.ok(name);
            } else {
                logger.warn("User not found for username: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The user '" + username + "' does not exist.");
            }
        } catch (Exception e) {
            logger.error("Error fetching user by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a problem. Please try again later.");
        }
    }
    @GetMapping("")
    public ResponseEntity<Object> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}