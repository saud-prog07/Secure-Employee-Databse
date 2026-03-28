package com.example.employee.service;

import com.example.employee.entity.AuditLog;
import com.example.employee.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for logging administrative actions on employee records.
 */
@Service
@Slf4j
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Logs an action with the performing user.
     *
     * @param action      The audit action (e.g., REGISTER_HR, LOGIN, etc.).
     * @param username    The username of the performing user.
     */
    public void logAction(String action, String username) {
        log.info("Logging audit action: {} by user: {}", action, username);
        
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUsername(username != null ? username : "SYSTEM");
        
        auditLogRepository.save(auditLog);
    }
}
