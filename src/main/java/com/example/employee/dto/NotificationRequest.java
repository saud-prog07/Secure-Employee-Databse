package com.example.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phoneNumber;
    
    @NotBlank(message = "Notification type is required")
    private String type; // registration, approval, rejection, otp, login, password-reset
    
    private String subject;
    private String body;
    private String otpCode;
    private String employeeName;
    private String approvedBy;
    private String rejectionReason;
    
    private boolean sendViaEmail = true;
    private boolean sendViaSMS = false;
}
