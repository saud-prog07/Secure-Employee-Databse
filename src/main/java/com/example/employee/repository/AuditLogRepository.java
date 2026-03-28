package com.example.employee.repository;

import com.example.employee.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for persisting AuditLog records.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
