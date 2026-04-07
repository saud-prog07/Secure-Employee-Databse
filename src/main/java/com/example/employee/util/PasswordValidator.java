package com.example.employee.util;

import org.springframework.stereotype.Component;

/**
 * Password validation utility for enforcing password strength requirements.
 * 
 * Requirements:
 * - Minimum 12 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (!@#$%^&*)
 */
@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 12;
    private static final String UPPERCASE_REGEX = ".*[A-Z].*";
    private static final String LOWERCASE_REGEX = ".*[a-z].*";
    private static final String DIGIT_REGEX = ".*[0-9].*";
    private static final String SPECIAL_CHAR_REGEX = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*";

    /**
     * Validates password strength
     * @param password the password to validate
     * @return ValidationResult containing success and error message
     */
    public ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH) {
            return new ValidationResult(false, 
                "Password must be at least " + MIN_LENGTH + " characters long (current: " + password.length() + ")");
        }

        if (!password.matches(UPPERCASE_REGEX)) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter (A-Z)");
        }

        if (!password.matches(LOWERCASE_REGEX)) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter (a-z)");
        }

        if (!password.matches(DIGIT_REGEX)) {
            return new ValidationResult(false, "Password must contain at least one digit (0-9)");
        }

        if (!password.matches(SPECIAL_CHAR_REGEX)) {
            return new ValidationResult(false, 
                "Password must contain at least one special character (!@#$%^&*()_+-=[]{};':\"\\|,.<>/?))");
        }

        return new ValidationResult(true, "Password is strong");
    }

    /**
     * Struct for validation result
     */
    public static class ValidationResult {
        public final boolean success;
        public final String message;

        public ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
