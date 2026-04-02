-- Payroll System Database Migration
-- This script adds the payroll table to support salary management

-- Create payroll table
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
    CONSTRAINT fk_employee_id FOREIGN KEY (employee_id) REFERENCES employees(id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_month (month),
    UNIQUE KEY uk_employee_month (employee_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify the table was created
SELECT 'Payroll table created successfully' AS result;
