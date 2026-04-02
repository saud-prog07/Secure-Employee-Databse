-- Two-Factor Authentication Database Migration Script
-- Run this script to add 2FA support to your Employee Management System database
-- Date: 2024

-- Add 2FA columns to users table
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE users ADD COLUMN two_factor_secret VARCHAR(255) NULL;

-- Add index for faster lookups (optional but recommended)
CREATE INDEX idx_two_factor_enabled ON users(two_factor_enabled);

-- Verify the changes
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users' AND COLUMN_NAME IN ('two_factor_enabled', 'two_factor_secret');

-- Optional: Update audit_log table to track 2FA events
-- This assumes an audit_log table exists with columns: id, username, action, timestamp

-- The following 2FA-related actions will be logged:
-- - LOGIN_AWAITING_OTP: User logged in successfully but 2FA required
-- - OTP_VERIFICATION_FAILED: User provided invalid OTP
-- - LOGIN_2FA_VERIFIED: User verified OTP and completed login
-- - 2FA_SETUP_CONFIRMED: User enabled 2FA
-- - 2FA_DISABLED: User disabled 2FA

-- If audit_log table doesn't exist, it should already be created by the application
-- using Liquibase or similar migration tool

-- Confirm migration success by running:
SELECT COUNT(*) as total_users, 
       SUM(CASE WHEN two_factor_enabled = TRUE THEN 1 ELSE 0 END) as two_fa_enabled_count
FROM users;
