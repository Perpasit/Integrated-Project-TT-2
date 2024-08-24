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

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            logger.warn("User not found for username: {}", username);
            return null;
        }
    }

    public String getNameByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user.getName();
        } else {
            logger.warn("User not found for username: {}", username);
            return null;
        }
    }

    public boolean validateUserCredentials(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found for username: {}", username);
            return false;
        }

        boolean isPasswordValid = verifyPassword(password, user.getPassword());
        if (isPasswordValid) {
            logger.info("User credentials validated successfully for username: {}", username);
        } else {
            logger.warn("Invalid password for username: {}", username);
        }

        return isPasswordValid;
    }

    // Other methods (createUser, updateUser, deleteUser) can be uncommented as needed
}
