package com.example.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username or Email is mandatory")
    @Size(min = 3, max = 100, message = "Username or Email must be between 3 and 100 characters")
    private String identifier;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 1, max = 500, message = "Password must be between 1 and 500 characters")
    private String password;
}
