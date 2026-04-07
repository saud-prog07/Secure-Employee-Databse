package com.example.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token is mandatory")
    private String token;

    @NotBlank(message = "New password is mandatory")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
