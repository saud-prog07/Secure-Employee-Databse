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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final com.example.employee.service.AuditLogService auditLogService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          AuthenticationManager authenticationManager,
                          com.example.employee.service.AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
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
        
        // Default role is HR if not specified
        user.setRole(request.getRole() != null ? request.getRole() : com.example.employee.entity.Role.HR);
        user.setApproved(false);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);
        
        // Audit log the registration
        auditLogService.logAction("REGISTER_HR", user.getUsername());

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
     * @return A ResponseEntity indicating successful login.
     */
    @Operation(summary = "Login a user", description = "Authenticates a user with username and password.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Process: Login. Attempting user: {}", request.getUsername());

        // Check for user existence
        var userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            log.warn("Login rejected: User '{}' not found", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
        }

        User user = userOpt.get();

        if (user.isDeleted()) {
            log.warn("Login rejected: Account '{}' is deactivated", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Account has been deactivated"));
        }
        if (!user.isApproved()) {
            log.warn("Login rejected: Account '{}' is pending approval", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Account pending admin approval"));
        }

        try {
            log.debug("Authenticating user: {}", request.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Audit log the login
            auditLogService.logAction("LOGIN", request.getUsername());

            // Return user details (without password)
            user.setPassword("********");
            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Login successful", user));

        } catch (Exception e) {
            log.warn("Login failed for user '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }
}
