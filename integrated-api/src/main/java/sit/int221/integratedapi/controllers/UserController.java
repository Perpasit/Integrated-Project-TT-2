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
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.services.UserService;
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



    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody JwtRequestUser jwtRequestUser) {
        try {
            // Fetch user by username
            User user = userService.getUserByUsername(jwtRequestUser.getUserName());
            if (user == null) {
                throw new CustomAuthenticationException("You do not have permission to access this resource.");
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
            
            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.error("Login error: ", e);
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There is a problem. Please try again later.");
        }
    }

    @PostMapping("/token")
    public ResponseEntity<Object> refreshAccessToken(@RequestHeader("Authorization") String requestTokenHeader) {
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String refreshToken = requestTokenHeader.substring(7);  // ตัดคำว่า "Bearer " ออก
            try {
                // ตรวจสอบว่า refresh token ถูกต้องหรือไม่
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(refreshToken);

                // ตรวจสอบว่า token_type เป็น "refresh"
                String tokenType = claims.get("token_type", String.class);
                if (!"refresh".equals(tokenType)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
                }

                // ดึงข้อมูลผู้ใช้จาก refresh token (เช่น OID หรือ Username)
                String username = claims.getSubject();
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);

                // ถ้า userDetails เป็น null หรือ user ถูกลบออกไปแล้ว
                if (userDetails == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User no longer exists");
                }

                // สร้าง access token ใหม่
                String newAccessToken = jwtTokenUtil.generateToken(userDetails);

                // ส่ง access token ใหม่กลับไป
                Map<String, String> response = new HashMap<>();
                response.put("access_token", newAccessToken);

                return ResponseEntity.ok(response);

            } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Refresh token error: ", e);
                throw new CustomAuthenticationException("Invalid refresh token or error during token processing");
            }
        } else {
            throw new IllegalArgumentException("Refresh token does not begin with Bearer String");
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
