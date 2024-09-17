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
import sit.int221.integratedapi.dtos.UserDTO;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.AuthenticationException;
import sit.int221.integratedapi.services.TaskUserService;
import sit.int221.integratedapi.services.UserService;
import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.services.JwtUserDetailsService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/login")
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

    @PostMapping("")
    public ResponseEntity<Object> login(@Valid @RequestBody JwtRequestUser jwtRequestUser) {
        try {
            User user = userService.getUserByUsername(jwtRequestUser.getUserName());
            if (user == null) {
                return AuthenticationException.getMessage("The Username or Password is incorrect.");
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(jwtRequestUser.getUserName(), jwtRequestUser.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails);

            // Extract claims from the token
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);

            // Create or update TaskUser
            TaskUser taskUser = new TaskUser();
            taskUser.setOid(user.getOid()); // Set OID from User entity
            taskUser.setUsername(jwtRequestUser.getUserName());
            taskUser.setName(user.getName()); // You might need to fetch user details from the User entity
            taskUser.setEmail(user.getEmail()); // You might need to fetch user details from the User entity
            taskUser.setRole(user.getRole()); // You might need to fetch user details from the User entity
            taskUser.setToken(token);
            taskUser.setCreatedOn(java.time.LocalDateTime.now().toString()); // Set current timestamp

            // Save or update TaskUser
            taskUserService.saveOrUpdateTaskUser(taskUser);

            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("access_token", token);

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