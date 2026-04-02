package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for authentication responses.
 * Handles both JWT token responses and OTP_REQUIRED status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String status; // "SUCCESS" or "OTP_REQUIRED"
    private String token; // JWT token (if 2FA not enabled or OTP verified)
    private String type = "Bearer";
    private String username;
    private List<String> roles;
    private boolean twoFactorEnabled;

    // Constructor for OTP required response
    public AuthResponse(String username, boolean twoFactorEnabled) {
        this.status = "OTP_REQUIRED";
        this.username = username;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    // Constructor for successful login response
    public AuthResponse(String token, String username, List<String> roles) {
        this.status = "SUCCESS";
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.twoFactorEnabled = false;
    }
}
