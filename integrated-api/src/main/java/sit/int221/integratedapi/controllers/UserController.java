package sit.int221.integratedapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.UserDTO;
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
    public String login(@RequestBody UserDTO userDTO) {
        String storedHashedPassword = userRepository.findPasswordByUsername(userDTO.getUsername());

        if (storedHashedPassword == null) {
            return "User not found";
        }

        boolean isPasswordValid = userService.verifyPassword(userDTO.getPassword(), storedHashedPassword);

        if (isPasswordValid) {
            return "Login successful";
        } else {
            return "Invalid credentials";
        }
    }
}
