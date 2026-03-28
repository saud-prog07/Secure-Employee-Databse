package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.entity.User;
import com.example.employee.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final com.example.employee.service.AuditLogService auditLogService;

    public AdminUserController(UserRepository userRepository, 
                               com.example.employee.service.AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
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

}
