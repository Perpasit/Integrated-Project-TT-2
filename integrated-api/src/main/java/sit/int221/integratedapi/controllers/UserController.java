package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.UserDTO;
import sit.int221.integratedapi.exceptions.AuthenticationException;
import sit.int221.integratedapi.services.UserService;
import sit.int221.integratedapi.repositories.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserDTO userDTO) {
        String storedHashedPassword = userRepository.findPasswordByUsername(userDTO.getUsername());

        if (storedHashedPassword == null || !userService.verifyPassword(userDTO.getPassword(), storedHashedPassword)) {
            throw new AuthenticationException("Username or Password is incorrect");
        }

        return ResponseEntity.ok("Login successful");
    }
}
