package com.example.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SMSService smsService;

    public NotificationService(EmailService emailService, SMSService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Send registration notification
     */
    public void sendRegistrationNotification(String email, String username) {
        try {
            log.info("Sending registration notification to: {}", email);
            boolean emailSent = emailService.sendRegistrationEmail(email, username);

            if (emailSent) {
                log.info("Registration email sent successfully to: {}", email);
            } else {
                log.warn("Failed to send registration email to: {}", email);
            }
        } catch (Exception e) {
            log.error("Error sending registration notification", e);
        }
    }

    /**
     * Send OTP via email and optionally SMS
     */
    public void sendOtpNotification(String email, String phoneNumber, String otp, boolean sendViaEmail, boolean sendViaSMS) {
        try {
            log.info("Sending OTP notification to email: {}, phone: {}", email, phoneNumber);

            if (sendViaEmail && email != null && !email.isEmpty()) {
                boolean emailSent = emailService.sendOtpEmail(email, otp);
                if (emailSent) {
                    log.info("OTP email sent successfully to: {}", email);
                } else {
                    log.warn("Failed to send OTP email to: {}", email);
                }
            }

            if (sendViaSMS && phoneNumber != null && !phoneNumber.isEmpty()) {
                boolean smsSent = smsService.sendOtpSMS(phoneNumber, otp);
                if (smsSent) {
                    log.info("OTP SMS sent successfully to: {}", phoneNumber);
                } else {
                    log.warn("Failed to send OTP SMS to: {}", phoneNumber);
                }
            }
        } catch (Exception e) {
            log.error("Error sending OTP notification", e);
        }
    }

    /**
     * Send approval notification
     */
    public void sendApprovalNotification(String email, String phoneNumber, String employeeName, String approvedBy, boolean sendViaEmail, boolean sendViaSMS) {
        try {
            log.info("Sending approval notification to: {}", employeeName);

            if (sendViaEmail && email != null && !email.isEmpty()) {
                boolean emailSent = emailService.sendApprovalEmail(email, employeeName, approvedBy);
                if (emailSent) {
                    log.info("Approval email sent successfully to: {}", email);
                } else {
                    log.warn("Failed to send approval email to: {}", email);
                }
            }

            if (sendViaSMS && phoneNumber != null && !phoneNumber.isEmpty()) {
                boolean smsSent = smsService.sendApprovalSMS(phoneNumber, employeeName);
                if (smsSent) {
                    log.info("Approval SMS sent successfully to: {}", phoneNumber);
                } else {
                    log.warn("Failed to send approval SMS to: {}", phoneNumber);
                }
            }
        } catch (Exception e) {
            log.error("Error sending approval notification", e);
        }
    }

    /**
     * Send rejection notification
     */
    public void sendRejectionNotification(String email, String phoneNumber, String employeeName, String rejectionReason, boolean sendViaEmail, boolean sendViaSMS) {
        try {
            log.info("Sending rejection notification to: {}", employeeName);

            if (sendViaEmail && email != null && !email.isEmpty()) {
                boolean emailSent = emailService.sendRejectionEmail(email, employeeName, rejectionReason);
                if (emailSent) {
                    log.info("Rejection email sent successfully to: {}", email);
                } else {
                    log.warn("Failed to send rejection email to: {}", email);
                }
            }

            if (sendViaSMS && phoneNumber != null && !phoneNumber.isEmpty()) {
                boolean smsSent = smsService.sendRejectionSMS(phoneNumber, employeeName);
                if (smsSent) {
                    log.info("Rejection SMS sent successfully to: {}", phoneNumber);
                } else {
                    log.warn("Failed to send rejection SMS to: {}", phoneNumber);
                }
            }
        } catch (Exception e) {
            log.error("Error sending rejection notification", e);
        }
    }

    /**
     * Send login notification (optional)
     */
    public void sendLoginNotification(String email, String phoneNumber, String username, boolean sendViaEmail, boolean sendViaSMS) {
        try {
            log.info("Sending login notification for user: {}", username);

            if (sendViaEmail && email != null && !email.isEmpty()) {
                String subject = "Login Alert";
                String message = String.format("Someone logged into your account (%s). If this wasn't you, please secure your account immediately.", username);
                boolean emailSent = emailService.sendSimpleEmail(email, subject, message);
                if (emailSent) {
                    log.info("Login email sent successfully to: {}", email);
                }
            }

            if (sendViaSMS && phoneNumber != null && !phoneNumber.isEmpty()) {
                boolean smsSent = smsService.sendLoginNotificationSMS(phoneNumber, username);
                if (smsSent) {
                    log.info("Login SMS sent successfully to: {}", phoneNumber);
                }
            }
        } catch (Exception e) {
            log.error("Error sending login notification", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetNotification(String email, String resetLink) {
        try {
            log.info("Sending password reset notification to: {}", email);
            boolean emailSent = emailService.sendPasswordResetEmail(email, resetLink);

            if (emailSent) {
                log.info("Password reset email sent successfully to: {}", email);
            } else {
                log.warn("Failed to send password reset email to: {}", email);
            }
        } catch (Exception e) {
            log.error("Error sending password reset notification", e);
        }
    }
}
