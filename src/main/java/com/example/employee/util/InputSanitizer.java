package com.example.employee.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user inputs to prevent XSS and injection attacks.
 * Removes or escapes potentially dangerous characters and patterns.
 */
@Component
@Slf4j
public class InputSanitizer {

    // Patterns for dangerous content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(')|(\"|\\\\)|(--|;)|(\\*|\\+|!=|>|<|=)|(union|select|insert|update|delete|drop|create|alter)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile("[;|&`$()]");
    
    /**
     * Sanitizes a string input by removing script tags and encoding HTML entities.
     *
     * @param input The string to sanitize
     * @return Sanitized string, or null if input is null
     */
    public String sanitizeString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove script tags
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Remove HTML tags (or just escape them)
        sanitized = encodeHtmlEntities(sanitized);
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }

    /**
     * Sanitizes email input.
     *
     * @param email The email to validate and sanitize
     * @return Sanitized email in lowercase, or null if invalid
     */
    public String sanitizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        // Convert to lowercase and trim
        String sanitized = email.toLowerCase().trim();
        
        // Validate email format (basic check)
        if (!sanitized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")) {
            log.warn("Invalid email format detected: {}", email);
            return null;
        }
        
        return sanitized;
    }

    /**
     * Sanitizes username input.
     *
     * @param username The username to sanitize
     * @return Sanitized username
     */
    public String sanitizeUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        // Remove any non-alphanumeric characters except dots, underscores, and hyphens
        String sanitized = username.replaceAll("[^a-zA-Z0-9._-]", "");
        
        // Trim to reasonable length
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        return sanitized.isEmpty() ? null : sanitized;
    }

    /**
     * Encodes HTML entities in a string to prevent XSS attacks.
     *
     * @param input The string to encode
     * @return HTML-encoded string
     */
    private String encodeHtmlEntities(String input) {
        if (input == null) {
            return null;
        }
        
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Checks if input contains potential SQL injection patterns.
     *
     * @param input The input to check
     * @return true if potentially malicious SQL patterns are detected
     */
    public boolean containsSqlInjectionPatterns(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Checks if input contains potential command injection patterns.
     *
     * @param input The input to check
     * @return true if potentially malicious command patterns are detected
     */
    public boolean containsCommandInjectionPatterns(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        return COMMAND_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Removes all non-alphanumeric characters except spaces.
     *
     * @param input The input string
     * @return Sanitized string with only alphanumeric characters and spaces
     */
    public String sanitizeAlphanumeric(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return input.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    /**
     * Sanitizes phone number - keeps only digits, +, -, and spaces.
     *
     * @param phone The phone number to sanitize
     * @return Sanitized phone number
     */
    public String sanitizePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        
        // Keep only digits, plus sign, hyphen, and spaces
        String sanitized = phone.replaceAll("[^0-9+\\-\\s]", "");
        
        // Trim to reasonable length (20 chars should be enough for international numbers)
        if (sanitized.length() > 20) {
            sanitized = sanitized.substring(0, 20);
        }
        
        return sanitized.isEmpty() ? null : sanitized;
    }

    /**
     * Validates and sanitizes a URL.
     *
     * @param url The URL to validate and sanitize
     * @return Sanitized URL, or null if invalid
     */
    public String sanitizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Check if URL contains valid protocols
        if (!url.matches("^https?://.*")) {
            log.warn("Invalid URL protocol detected: {}", url);
            return null;
        }

        try {
            new java.net.URL(url);
            return url;
        } catch (java.net.MalformedURLException e) {
            log.warn("Malformed URL detected: {}", url);
            return null;
        }
    }

    /**
     * Sanitizes multiline text input (like descriptions, comments).
     *
     * @param text The text to sanitize
     * @return Sanitized text with script tags removed
     */
    public String sanitizeMultilineText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Remove script tags
        String sanitized = SCRIPT_PATTERN.matcher(text).replaceAll("");
        
        // Encode HTML entities but preserve line breaks
        sanitized = sanitized.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
        
        return sanitized;
    }

    /**
     * Checks if string length is within acceptable bounds.
     *
     * @param input The input string
     * @param maxLength The maximum allowed length
     * @return true if length is valid
     */
    public boolean isValidLength(String input, int maxLength) {
        if (input == null) {
            return true; // null is acceptable for optional fields
        }
        return input.length() <= maxLength;
    }
}
