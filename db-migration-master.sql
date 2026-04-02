-- ==================================================================================
-- CONSOLIDATED DATABASE MIGRATION SCRIPT
-- Employee Management System - All Features in One File
-- ==================================================================================
-- This script consolidates all database migrations into a single file:
-- - 2FA Authentication
-- - Employee Approval Workflow
-- - Attendance Tracking
-- - Payroll System
-- - Workday Statistics (Holidays)
--
-- Date: April 2, 2026
-- ==================================================================================


-- ==================================================================================
-- SECTION 1: TWO-FACTOR AUTHENTICATION (2FA)
-- ==================================================================================
-- Add 2FA support to users table

ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE users ADD COLUMN two_factor_secret VARCHAR(255) NULL;

-- Add index for faster lookups
CREATE INDEX idx_two_factor_enabled ON users(two_factor_enabled);

-- The following 2FA-related actions will be logged:
-- - LOGIN_AWAITING_OTP: User logged in successfully but 2FA required
-- - OTP_VERIFICATION_FAILED: User provided invalid OTP
-- - LOGIN_2FA_VERIFIED: User verified OTP and completed login
-- - 2FA_SETUP_CONFIRMED: User enabled 2FA
-- - 2FA_DISABLED: User disabled 2FA


-- ==================================================================================
-- SECTION 2: APPROVAL WORKFLOW FOR EMPLOYEES
-- ==================================================================================
-- Add approval tracking fields and status update to handle workflow approvals

ALTER TABLE employees 
ADD COLUMN approved_by VARCHAR(255) NULL COMMENT 'Username of the admin who approved/rejected the employee',
ADD COLUMN approved_at TIMESTAMP NULL COMMENT 'Timestamp when the employee was approved or rejected',
MODIFY COLUMN status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT 'Employee approval status: PENDING, APPROVED, or REJECTED';

-- Add indexes on approval-related columns for faster queries
ALTER TABLE employees 
ADD INDEX idx_status (status),
ADD INDEX idx_approved_at (approved_at),
ADD INDEX idx_approved_by (approved_by);

-- Create an audit trail view for approval actions
CREATE VIEW approval_audit_view AS
SELECT 
    a.id,
    a.name,
    a.email,
    a.department,
    a.status,
    a.approved_by,
    a.approved_at,
    a.created_at,
    CASE 
        WHEN a.status = 'APPROVED' THEN DATEDIFF(a.approved_at, a.created_at)
        ELSE NULL 
    END AS days_to_approval,
    al.action,
    al.username,
    al.timestamp
FROM employees a
LEFT JOIN audit_logs al ON (
    (al.action = 'APPROVE_EMPLOYEE' OR al.action = 'REJECT_EMPLOYEE') 
    AND al.username = a.approved_by
)
WHERE a.deleted = FALSE
ORDER BY a.created_at DESC;

-- Update any existing employees that don't have status set
UPDATE employees 
SET status = 'APPROVED', approved_by = 'SYSTEM', approved_at = NOW()
WHERE status IS NULL OR status = '';


-- ==================================================================================
-- SECTION 3: ATTENDANCE TRACKING SYSTEM
-- ==================================================================================
-- Creates attendance table for QR-based attendance tracking

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

-- Create a view for daily attendance summary
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


-- ==================================================================================
-- SECTION 4: PAYROLL SYSTEM
-- ==================================================================================
-- Creates payroll table to support salary management

CREATE TABLE IF NOT EXISTS payroll (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    base_salary DECIMAL(10, 2) NOT NULL,
    bonus DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    deductions DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    final_salary DECIMAL(10, 2) NOT NULL,
    month VARCHAR(7) NOT NULL COMMENT 'Format: YYYY-MM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payroll_employee_id FOREIGN KEY (employee_id) REFERENCES employees(id),
    INDEX idx_payroll_employee_id (employee_id),
    INDEX idx_payroll_month (month),
    UNIQUE KEY uk_payroll_employee_month (employee_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==================================================================================
-- SECTION 5: WORKDAY STATISTICS & HOLIDAYS
-- ==================================================================================
-- Creates holidays table for workday calculations

CREATE TABLE IF NOT EXISTS holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_holiday_date (date),
    INDEX idx_holiday_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert 2024 holidays (United States - Common holidays)
INSERT INTO holidays (date, name, description, year) VALUES
('2024-01-01', 'New Year Day', 'New Year holiday', 2024),
('2024-01-15', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2024),
('2024-02-19', 'Presidents Day', 'Presidents Day', 2024),
('2024-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2024),
('2024-05-27', 'Memorial Day', 'Memorial Day', 2024),
('2024-07-04', 'Independence Day', 'Independence Day', 2024),
('2024-09-02', 'Labor Day', 'Labor Day', 2024),
('2024-10-14', 'Columbus Day', 'Columbus Day', 2024),
('2024-11-11', 'Veterans Day', 'Veterans Day', 2024),
('2024-11-28', 'Thanksgiving', 'Thanksgiving Day', 2024),
('2024-11-29', 'Day After Thanksgiving', 'Day after Thanksgiving', 2024),
('2024-12-25', 'Christmas', 'Christmas Day', 2024);

-- Insert 2025 holidays
INSERT INTO holidays (date, name, description, year) VALUES
('2025-01-01', 'New Year Day', 'New Year holiday', 2025),
('2025-01-20', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2025),
('2025-02-17', 'Presidents Day', 'Presidents Day', 2025),
('2025-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2025),
('2025-05-26', 'Memorial Day', 'Memorial Day', 2025),
('2025-07-04', 'Independence Day', 'Independence Day', 2025),
('2025-09-01', 'Labor Day', 'Labor Day', 2025),
('2025-10-13', 'Columbus Day', 'Columbus Day', 2025),
('2025-11-11', 'Veterans Day', 'Veterans Day', 2025),
('2025-11-27', 'Thanksgiving', 'Thanksgiving Day', 2025),
('2025-11-28', 'Day After Thanksgiving', 'Day after Thanksgiving', 2025),
('2025-12-25', 'Christmas', 'Christmas Day', 2025);

-- Insert 2026 holidays
INSERT INTO holidays (date, name, description, year) VALUES
('2026-01-01', 'New Year Day', 'New Year holiday', 2026),
('2026-01-19', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2026),
('2026-02-16', 'Presidents Day', 'Presidents Day', 2026),
('2026-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2026),
('2026-05-25', 'Memorial Day', 'Memorial Day', 2026),
('2026-07-04', 'Independence Day', 'Independence Day', 2026),
('2026-09-07', 'Labor Day', 'Labor Day', 2026),
('2026-10-12', 'Columbus Day', 'Columbus Day', 2026),
('2026-11-11', 'Veterans Day', 'Veterans Day', 2026),
('2026-11-26', 'Thanksgiving', 'Thanksgiving Day', 2026),
('2026-11-27', 'Day After Thanksgiving', 'Day after Thanksgiving', 2026),
('2026-12-25', 'Christmas', 'Christmas Day', 2026);

-- Insert 2027 holidays
INSERT INTO holidays (date, name, description, year) VALUES
('2027-01-01', 'New Year Day', 'New Year holiday', 2027),
('2027-01-18', 'MLK Jr. Day', 'Martin Luther King Jr. Birthday', 2027),
('2027-02-15', 'Presidents Day', 'Presidents Day', 2027),
('2027-03-17', 'St. Patricks Day', 'St. Patrick''s Day', 2027),
('2027-05-31', 'Memorial Day', 'Memorial Day', 2027),
('2027-07-04', 'Independence Day', 'Independence Day', 2027),
('2027-09-06', 'Labor Day', 'Labor Day', 2027),
('2027-10-11', 'Columbus Day', 'Columbus Day', 2027),
('2027-11-11', 'Veterans Day', 'Veterans Day', 2027),
('2027-11-25', 'Thanksgiving', 'Thanksgiving Day', 2027),
('2027-11-26', 'Day After Thanksgiving', 'Day after Thanksgiving', 2027),
('2027-12-25', 'Christmas', 'Christmas Day', 2027);


-- ==================================================================================
-- MIGRATION VERIFICATION
-- ==================================================================================
-- Verify all tables and modifications were created successfully

SELECT 'Migration Verification Results:' AS section;

-- Check 2FA columns
SELECT 'Users table 2FA columns' AS check_name, 
       COUNT(*) AS column_count
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users' 
AND COLUMN_NAME IN ('two_factor_enabled', 'two_factor_secret');

-- Check employees approval columns
SELECT 'Employees approval columns' AS check_name,
       COUNT(*) AS column_count
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'employees'
AND COLUMN_NAME IN ('approved_by', 'approved_at', 'status');

-- Check attendance table
SELECT 'Attendance table exists' AS check_name,
       COUNT(*) AS result
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'attendance';

-- Check payroll table
SELECT 'Payroll table exists' AS check_name,
       COUNT(*) AS result
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'payroll';

-- Check holidays table
SELECT 'Holidays table exists' AS check_name,
       COUNT(*) AS result
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME = 'holidays';

-- Check holiday counts by year
SELECT 'Holiday records by year' AS check_name,
       year,
       COUNT(*) as count
FROM holidays
GROUP BY year
ORDER BY year;

SELECT 'All migrations completed successfully!' AS final_status;

-- ==================================================================================
-- END OF MIGRATION SCRIPT
-- ==================================================================================
