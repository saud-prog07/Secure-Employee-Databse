package com.example.employee.controller;

import com.example.employee.dto.ApiResponse;
import com.example.employee.dto.EmailRequest;
import com.example.employee.dto.SMSRequest;
import com.example.employee.service.EmailService;
import com.example.employee.service.SMSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
@Tag(name = "Notifications", description = "Manage notifications (Email and SMS)")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final EmailService emailService;
    private final SMSService smsService;

    public NotificationController(EmailService emailService, SMSService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Send a custom email
     * ADMIN only
     */
    @Operation(summary = "Send custom email", description = "Send a custom email to a recipient (ADMIN only)")
    @PostMapping("/email/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> sendEmail(@Valid @RequestBody EmailRequest request) {
        try {
            log.info("Sending email to: {}", request.getTo());
            
            boolean sent = request.isHtml() 
                ? emailService.sendHtmlEmail(request.getTo(), request.getSubject(), request.getBody())
                : emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getBody());
            
            if (sent) {
                log.info("Email sent successfully to: {}", request.getTo());
                return ResponseEntity.ok(ApiResponse.success("Email sent successfully", request.getTo()));
            } else {
                log.warn("Failed to send email to: {}", request.getTo());
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to send email"));
            }
        } catch (Exception e) {
            log.error("Error sending email", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error sending email: " + e.getMessage()));
        }
    }

    /**
     * Send a custom SMS
     * ADMIN only
     */
    @Operation(summary = "Send custom SMS", description = "Send a custom SMS to a phone number (ADMIN only)")
    @PostMapping("/sms/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> sendSMS(@Valid @RequestBody SMSRequest request) {
        try {
            log.info("Sending SMS to: {}", request.getPhoneNumber());
            
            boolean sent = smsService.sendSMS(request.getPhoneNumber(), request.getMessage());
            
            if (sent) {
                log.info("SMS sent successfully to: {}", request.getPhoneNumber());
                return ResponseEntity.ok(ApiResponse.success("SMS sent successfully", request.getPhoneNumber()));
            } else {
                log.warn("Failed to send SMS to: {}", request.getPhoneNumber());
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to send SMS"));
            }
        } catch (Exception e) {
            log.error("Error sending SMS", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error sending SMS: " + e.getMessage()));
        }
    }

    /**
     * Test email endpoint (for development)
     */
    @Operation(summary = "Test email", description = "Send a test email to verify configuration")
    @PostMapping("/test/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> testEmail(@PathVariable String email) {
        try {
            log.info("Sending test email to: {}", email);
            
            boolean sent = emailService.sendSimpleEmail(
                email, 
                "Test Email - Employee Management System",
                "This is a test email from the Employee Management System. If you received this, email notifications are working correctly."
            );
            
            if (sent) {
                log.info("Test email sent successfully to: {}", email);
                return ResponseEntity.ok(ApiResponse.success("Test email sent successfully", email));
            } else {
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to send test email"));
            }
        } catch (Exception e) {
            log.error("Error sending test email", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Test SMS endpoint (for development)
     */
    @Operation(summary = "Test SMS", description = "Send a test SMS to verify configuration")
    @PostMapping("/test/sms/{phoneNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> testSMS(@PathVariable String phoneNumber) {
        try {
            log.info("Sending test SMS to: {}", phoneNumber);
            
            boolean sent = smsService.sendSMS(
                phoneNumber,
                "This is a test SMS from the Employee Management System. If you received this, SMS notifications are working correctly."
            );
            
            if (sent) {
                log.info("Test SMS sent successfully to: {}", phoneNumber);
                return ResponseEntity.ok(ApiResponse.success("Test SMS sent successfully", phoneNumber));
            } else {
                return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to send test SMS"));
            }
        } catch (Exception e) {
            log.error("Error sending test SMS", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }
}
