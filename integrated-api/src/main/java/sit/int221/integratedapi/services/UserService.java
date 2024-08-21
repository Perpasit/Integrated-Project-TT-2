package sit.int221.integratedapi.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.UserDTO;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.repositories.login.UserRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.util.List;
import java.util.Optional;

@Service
@Qualifier("loginDataSource")
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final Argon2 argon2;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserService() {
        this.argon2 = Argon2Factory.create();
    }

    // Method to hash password
    public String hashPassword(String password) {
        try {
            String hashedPassword = argon2.hash(12, 65536, 1, password);
            // Update parameters here if needed
            logger.info("Generated Hashed Password: {}", hashedPassword);
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            return null;
        }
    }

    // Method to verify password
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            boolean isValid = argon2.verify(hashedPassword, password);
            logger.info("Password verification result for password '{}': {}", password, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        logger.debug("Users fetched from repository: {}", users);
        return ListMapper.mapList(users, UserDTO.class);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return new UserDTO(user.getOid(), user.getUsername());
        } else {
            logger.warn("User not found for username: {}", username);
            return null;
        }
    }

//    public void createUser(UserDTO userDTO) {
//        User user = User.builder()
//                .oid(userDTO.getOid())
//                .username(userDTO.getUsername())
//                .name(userDTO.getName())
//                .email(userDTO.getEmail())
//                .password(hashPassword(userDTO.getPassword()))
//                .role(userDTO.getRole())
//                .build();
//        userRepository.save(user);
//        logger.info("User created successfully: {}", user);
//    }

//    public void updateUser(UserDTO userDTO) {
//        Optional<User> userOptional = userRepository.findByUsername(userDTO.getUsername());
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            user.setName(userDTO.getName());
//            user.setEmail(userDTO.getEmail());
//            user.setRole(userDTO.getRole());
//            if (userDTO.getPassword() != null) {
//                user.setPassword(hashPassword(userDTO.getPassword()));
//            }
//            userRepository.save(user);
//            logger.info("User updated successfully: {}", user);
//        } else {
//            logger.warn("User not found for update: {}", userDTO.getUsername());
//        }
//    }
//
//    public void deleteUser(String username) {
//        Optional<User> userOptional = userRepository.findByUsername(username);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            userRepository.delete(user);
//            logger.info("User deleted successfully: {}", user);
//        } else {
//            logger.warn("User not found for deletion: {}", username);
//        }
//    }
}