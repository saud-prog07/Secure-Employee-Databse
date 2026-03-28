package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final com.example.employee.repository.UserRepository userRepository;
    private final com.example.employee.service.AuditLogService auditLogService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AdminUserController(com.example.employee.repository.UserRepository userRepository, 
                               com.example.employee.service.AuditLogService auditLogService,
                               org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all users in the system.
     * Passwords are scrubbed for security.
     */
    @Operation(summary = "List all users", description = "Retrieves all users with scrubbed passwords. Only accessible by ADMIN.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("Admin Request: Listing all users");
        List<User> users = userRepository.findAll().stream()
                .map(user -> {
                    user.setPassword("********");
                    return user;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Approves a pending user account.
     */
    @Operation(summary = "Approve a user", description = "Sets the'approved' flag to true for a user. Only accessible by ADMIN.")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<User>> approveUser(@PathVariable Long id) {
        log.info("Admin Request: Approving user ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    user.setApproved(true);
                    User savedUser = userRepository.save(user);
                    
                    // Audit log the HR approval
                    auditLogService.logAction("APPROVE_HR", SecurityContextHolder.getContext().getAuthentication().getName());
                    
                    savedUser.setPassword("********");
                    return ResponseEntity.ok(ApiResponse.success("User approved", savedUser));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * Reverts a user account to unapproved status.
     */
    @Operation(summary = "Disapprove a user", description = "Sets the 'approved' flag to false for a user. Only accessible by ADMIN.")
    @PutMapping("/{id}/disapprove")
    public ResponseEntity<ApiResponse<User>> disapproveUser(@PathVariable Long id) {
        log.info("Admin Request: Disapproving user ID: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    user.setApproved(false);
                    User savedUser = userRepository.save(user);
                    
                    auditLogService.logAction("DISAPPROVE_HR", SecurityContextHolder.getContext().getAuthentication().getName());
                    
                    savedUser.setPassword("********");
                    return ResponseEntity.ok(ApiResponse.success("User disapproved", savedUser));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * Deactivates (soft deletes) a user account.
     */
    @Operation(summary = "Deactivate a user", description = "Sets the'deleted' flag to true. Cannot deactivate yourself. Only accessible by ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Admin Request: Deactivating user ID: {} by {}", id, currentUsername);

        return userRepository.findById(id)
                .map(user -> {
                    // Safety check: Cannot deactivate yourself
                    if (user.getUsername().equals(currentUsername)) {
                        log.warn("Admin '{}' attempted to deactivate themselves", currentUsername);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Void>error("Safety breach: You cannot deactivate your own account"));
                    }

                    user.setDeleted(true);
                    userRepository.save(user);
                    return ResponseEntity.ok(ApiResponse.<Void>success("User deactivated successfully", null));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * Creates a new user directly (Admin Action).
     * These users are auto-approved.
     */
    @Operation(summary = "Create user", description = "Directly creates a new user account. Auto-approved by default. Only accessible by ADMIN.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        log.info("Admin Request: Creating new user '{}'", user.getUsername());
        
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username already exists"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setApproved(true);
        user.setDeleted(false);
        
        User savedUser = userRepository.save(user);
        savedUser.setPassword("********");
        
        auditLogService.logAction("ADMIN_CREATE_USER", SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created", savedUser));
    }

    /**
     * PERMANENTLY deletes a user account (Hard Delete).
     */
    @Operation(summary = "Hard delete user", description = "PERMANENTLY removes a user from the database. WARNING: Cannot be undone. Only accessible by ADMIN.")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> hardDeleteUser(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Admin Request: Permanent deletion of user ID: {} by {}", id, currentUsername);

        return userRepository.findById(id)
                .map(user -> {
                    if (user.getUsername().equals(currentUsername)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Void>error("Safety breach: You cannot permanently delete yourself"));
                    }
                    userRepository.delete(user);
                    auditLogService.logAction("ADMIN_HARD_DELETE_USER", currentUsername);
                    return ResponseEntity.ok(ApiResponse.<Void>success("User permanently deleted", null));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User not found")));
    }
}
