package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.entity.AuditLog;
import com.example.employee.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit", description = "System Audit Logging API")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Retrieves all system audit logs.
     * Accessible only by ADMIN roles.
     */
    @Operation(summary = "List all audit logs", description = "Retrieves all system audit logs sorted by newest first.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        log.info("Admin Request: Fetching all system audit logs");
        List<AuditLog> logs = auditLogRepository.findAll();
        // Sort by timestamp descending
        logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }
}
