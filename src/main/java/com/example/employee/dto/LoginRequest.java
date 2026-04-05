package com.example.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username or Email is mandatory")
    private String identifier;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
