package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.LoginRequest;
import com.example.employee.dto.RegisterRequest;
import com.example.employee.entity.User;
import com.example.employee.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the specified role.
     *
     * @param request The registration details (username, password, role).
     * @return A ResponseEntity with the saved User object (password hidden).
     */
    @Operation(summary = "Register a new user", description = "Creates a new user account with BCrypt encrypted password.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Process: Registration. Target User: {}", request.getUsername());

        // Check for existing username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: Username '{}' is already taken", request.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Username already exists"));
        }

        // Map DTO to Entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        
        // Security: Never return the password
        savedUser.setPassword("********");

        log.info("Registration successful for user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", savedUser));
    }

    /**
     * Authenticates a user based on username and password.
     *
     * @param request The login credentials.
     * @return A ResponseEntity with user details if successful.
     */
    @Operation(summary = "Login a user", description = "Validates credentials and returns user information if successful.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Process: Login. Attempting user: {}", request.getUsername());

        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.info("Login successful for user: {}", request.getUsername());
                        user.setPassword("********"); // Hide password
                        return ResponseEntity.ok(ApiResponse.success("Login successful", user));
                    } else {
                        log.warn("Login failed: Invalid password for user '{}'", request.getUsername());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiResponse<User>("error", "Invalid username or password", null));
                    }
                })
                .orElseGet(() -> {
                    log.warn("Login failed: User '{}' not found", request.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ApiResponse<User>("error", "Invalid username or password", null));
                });
    }
}
