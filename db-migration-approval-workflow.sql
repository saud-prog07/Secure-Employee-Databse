-- Database Migration: Approval Workflow for Employees
-- Description: Add approval tracking fields and status update to handle workflow approvals

-- Add new columns to employees table
ALTER TABLE employees 
ADD COLUMN approved_by VARCHAR(255) NULL COMMENT 'Username of the admin who approved/rejected the employee',
ADD COLUMN approved_at TIMESTAMP NULL COMMENT 'Timestamp when the employee was approved or rejected',
MODIFY COLUMN status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT 'Employee approval status: PENDING, APPROVED, or REJECTED';

-- Add index on status for faster queries
ALTER TABLE employees 
ADD INDEX idx_status (status),
ADD INDEX idx_approved_at (approved_at),
ADD INDEX idx_approved_by (approved_by);

-- Create an audit trail for approval actions (optional, if you want to track changes)
-- This creates a view to show approval history
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
-- (Set approved by SYSTEM if already in database)
UPDATE employees 
SET status = 'APPROVED', approved_by = 'SYSTEM', approved_at = NOW()
WHERE status IS NULL OR status = '';
