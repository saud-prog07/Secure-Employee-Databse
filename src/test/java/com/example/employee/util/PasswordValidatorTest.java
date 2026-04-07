package com.example.employee.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordValidator Unit Tests")
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    @DisplayName("Should accept valid strong password")
    void testValidate_StrongPassword() {
        var result = passwordValidator.validate("SecurePass#2024");
        
        assertTrue(result.success);
        assertEquals("Password is strong", result.message);
    }

    @Test
    @DisplayName("Should accept another valid strong password")
    void testValidate_AnotherStrongPassword() {
        var result = passwordValidator.validate("MyDogRuns@2024Spring");
        
        assertTrue(result.success);
    }

    @Test
    @DisplayName("Should reject null password")
    void testValidate_NullPassword() {
        var result = passwordValidator.validate(null);
        
        assertFalse(result.success);
        assertTrue(result.message.contains("cannot be empty"));
    }

    @Test
    @DisplayName("Should reject empty password")
    void testValidate_EmptyPassword() {
        var result = passwordValidator.validate("");
        
        assertFalse(result.success);
        assertTrue(result.message.contains("cannot be empty"));
    }

    @Test
    @DisplayName("Should reject password shorter than 12 characters")
    void testValidate_TooShort() {
        var result = passwordValidator.validate("Pass#1");
        
        assertFalse(result.success);
        assertTrue(result.message.contains("12 characters"));
    }

    @Test
    @DisplayName("Should reject password without uppercase letter")
    void testValidate_NoUppercase() {
        var result = passwordValidator.validate("securepass#2024");
        
        assertFalse(result.success);
        assertTrue(result.message.contains("uppercase"));
    }

    @Test
    @DisplayName("Should reject password without lowercase letter")
    void testValidate_NoLowercase() {
        var result = passwordValidator.validate("SECUREPASS#2024");
        
        assertFalse(result.success);
        assertTrue(result.message.contains("lowercase"));
    }

    @Test
    @DisplayName("Should reject password without digit")
    void testValidate_NoDigit() {
        var result = passwordValidator.validate("SecurePass#Spring");
        
        assertFalse(result.success);
        assertTrue(result.message.contains("digit"));
    }

    @Test
    @DisplayName("Should reject password without special character")
    void testValidate_NoSpecialChar() {
        var result = passwordValidator.validate("SecurePass2024");
        
        assertFalse(result.success);
        assertTrue(result.message.contains("special character"));
    }

    @Test
    @DisplayName("Should accept various valid special characters")
    void testValidate_VariousSpecialChars() {
        var passwords = new String[]{
            "Pass!word2024",
            "Pass@word2024",
            "Pass#word2024",
            "Pass$word2024",
            "Pass%word2024",
            "Pass^word2024",
            "Pass&word2024",
            "Pass*word2024",
            "Pass(word2024",
            "Pass)word2024",
            "Pass+word2024",
            "Pass-word2024",
            "Pass=word2024",
            "Pass_word2024",
            "Pass[word2024",
            "Pass]word2024",
            "Pass{word2024",
            "Pass}word2024",
            "Pass;word2024",
            "Pass:word2024",
            "Pass'word2024",
            "Pass,word2024",
            "Pass.word2024",
            "Pass/word2024",
            "Pass?word2024"
        };

        for (String password : passwords) {
            var result = passwordValidator.validate(password);
            assertTrue(result.success, "Password should be valid: " + password);
        }
    }

    @Test
    @DisplayName("Should accept 12-character minimum password")
    void testValidate_ExactMinimumLength() {
        var result = passwordValidator.validate("Pass@Word2");  // 10 chars - too short
        assertFalse(result.success);
        
        var result2 = passwordValidator.validate("Pass@Word201");  // 12 chars - minimum
        assertTrue(result2.success);
    }

    @Test
    @DisplayName("Should accept very long passwords")
    void testValidate_VeryLongPassword() {
        var result = passwordValidator.validate("VeryLongSecurePassword@With#Lots$Of%Specials&2024");
        
        assertTrue(result.success);
    }
}
