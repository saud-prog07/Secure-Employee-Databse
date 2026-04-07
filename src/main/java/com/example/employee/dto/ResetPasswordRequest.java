package com.example.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token is mandatory")
    @Size(max = 500, message = "Token is invalid")
    private String token;

    @NotBlank(message = "New password is mandatory")
    @Size(min = 12, message = "Password must be at least 12 characters long")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])",
            message = "Password must contain uppercase, lowercase, digit, and special character")
    private String newPassword;
}
