package sit.int221.integratedapi.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.AuthenticationException;
import sit.int221.integratedapi.services.TaskUserService;
import sit.int221.integratedapi.services.UserService;
import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.services.JwtUserDetailsService;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
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

    @Autowired
    private TaskUserService taskUserService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody JwtRequestUser jwtRequestUser) {
        try {
            // Fetch user by username
            User user = userService.getUserByUsername(jwtRequestUser.getUserName());
            if (user == null) {
                return AuthenticationException.getMessage("The Username or Password is incorrect.");
            }

            // Authenticate the user
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(jwtRequestUser.getUserName(), jwtRequestUser.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // Retrieve UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate access_token (valid for 30 minutes)
            String accessToken = jwtTokenUtil.generateToken(userDetails);

            // Generate refresh_token (valid for 24 hours)
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // Create or update TaskUser (existing logic)
            TaskUser taskUser = new TaskUser();
            taskUser.setOid(user.getOid());
            taskUser.setUsername(jwtRequestUser.getUserName());
            taskUser.setName(user.getName());
            taskUser.setEmail(user.getEmail());
            taskUser.setCreatedOn(ZonedDateTime.now());
            taskUser.setUpdatedOn(ZonedDateTime.now());
            taskUserService.saveOrUpdateTaskUser(taskUser);

            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.error("Login error: ", e);
            return AuthenticationException.getMessage("The Username or Password is incorrect.");
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a problem. Please try again later.");
        }
    }

    @PostMapping("/token")
    public ResponseEntity<Object> refreshAccessToken(@RequestHeader("Authorization") String requestTokenHeader) {
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String refreshToken = requestTokenHeader.substring(7);
            try {
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(refreshToken);
                String username = claims.getSubject();

                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);

                // Validate the refresh token
                if (jwtTokenUtil.validateToken(refreshToken, userDetails)) {
                    String newAccessToken = jwtTokenUtil.generateToken(userDetails);

                    Map<String, String> response = new HashMap<>();
                    response.put("access_token", newAccessToken);

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
                }
            } catch (ExpiredJwtException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired", e);
            } catch (Exception e) {
                logger.error("Refresh token error: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("There is a problem. Please try again later.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token does not begin with Bearer String");
        }
    }


    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String requestTokenHeader) {
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
                return ResponseEntity.ok(claims);
            } catch (ExpiredJwtException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token has expired", e);
            } catch (MalformedJwtException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token is malformed", e);
            } catch (SignatureException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token has been tampered with", e);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token", e);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT Token does not begin with Bearer String");
        }
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
