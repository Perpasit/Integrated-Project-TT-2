package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.UserDTO;
import sit.int221.integratedapi.services.UserService;
import sit.int221.integratedapi.repositories.login.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserDTO userDTO) {
        try {
            // Retrieve the hashed password from the repository
            String storedHashedPassword = userRepository.findPasswordByUsername(userDTO.getUsername());

            if (storedHashedPassword == null) {
                logger.warn("Login attempt with non-existent username: {}", userDTO.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username or Password is incorrect");
            }

            // Log retrieved hashed password for debugging (be cautious in production)
            logger.debug("Retrieved Hashed Password from DB: {}", storedHashedPassword);
            logger.debug("Password Provided: {}", userDTO.getPassword());

            // Validate the password
            boolean isPasswordValid = userService.verifyPassword(userDTO.getPassword(), storedHashedPassword);

            if (isPasswordValid) {
                logger.info("Login successful for user: {}", userDTO.getUsername());
                return ResponseEntity.ok("Login successful");
            } else {
                logger.warn("Failed login attempt for user: {}", userDTO.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username or Password is incorrect");
            }
        } catch (Exception e) {
            logger.error("Error during login process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }




    @GetMapping("")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        logger.info("Users fetched from repository: {}", users);
        return ResponseEntity.ok(users);
    }
}
