-- Database Migration: Attendance System
-- Creates attendance table for QR-based attendance tracking
-- Date: 2026-04-01

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    login_time DATETIME NOT NULL,
    status ENUM('PRESENT', 'ABSENT') NOT NULL DEFAULT 'PRESENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY uk_employee_date (employee_id, date),
    INDEX idx_employee_date (employee_id, date),
    INDEX idx_date (date),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add column to audit_logs if needed (already exists)
-- The attendance system automatically logs all scans via AuditLogService

-- Verify the table was created
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'attendance';

-- Optional: Create a view for daily attendance summary
CREATE OR REPLACE VIEW attendance_daily_summary AS
SELECT 
    a.date,
    e.id as employee_id,
    e.name as employee_name,
    e.department,
    a.login_time,
    a.status,
    TIME_FORMAT(a.login_time, '%h:%i %p') as login_time_formatted
FROM attendance a
LEFT JOIN employees e ON a.employee_id = e.id
ORDER BY a.date DESC, a.login_time ASC;
