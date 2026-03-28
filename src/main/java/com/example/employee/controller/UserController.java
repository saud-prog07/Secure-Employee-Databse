package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.entity.User;
import com.example.employee.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@Slf4j
@Tag(name = "Profile", description = "User Profile Management API")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves the current logged-in user's details.
     */
    @Operation(summary = "Get current profile", description = "Fetches details of the currently authenticated user.")
    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setPassword("********");
                    return ResponseEntity.ok(ApiResponse.success("Profile fetched", user));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * Updates the password of the current user.
     */
    @Operation(summary = "Update password", description = "Allows the current user to change their password securely.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@RequestBody Map<String, String> request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String newPassword = request.get("newPassword");

        if (newPassword == null || newPassword.length() < 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password must be at least 5 characters long"));
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    log.info("Password updated for user: {}", username);
                    return ResponseEntity.ok(ApiResponse.<Void>success("Password updated successfully", null));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }
}
