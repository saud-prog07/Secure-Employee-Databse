package com.example.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a simple text email
     */
    public boolean sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setReplyTo(fromEmail);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return false;
        }
    }

    /**
     * Send HTML email
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setReplyTo(fromEmail);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
            return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return false;
        }
    }

    /**
     * Send registration confirmation email
     */
    public boolean sendRegistrationEmail(String email, String username) {
        String subject = "Welcome to Employee Management System";
        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Welcome, %s!</h2>
                    <p>Your account has been successfully created.</p>
                    <p><strong>Username:</strong> %s</p>
                    <p>You can now login to the Employee Management System.</p>
                    <p>If you have any questions, please contact our support team.</p>
                    <hr style="border: none; border-top: 1px solid #ddd;"/>
                    <p style="color: #7f8c8d; font-size: 12px;">
                        This is an automated email. Please do not reply directly.
                    </p>
                </div>
                </body>
                </html>
                """, username, username);

        return sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send approval notification email
     */
    public boolean sendApprovalEmail(String email, String employeeName, String approvedBy) {
        String subject = "Employee Update: Approval Completed";
        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #27ae60;">✓ Approval Approved</h2>
                    <p>Hi %s,</p>
                    <p>Your employee record has been <strong style="color: #27ae60;">APPROVED</strong>.</p>
                    <p><strong>Approved By:</strong> %s</p>
                    <p>You can now access all system features.</p>
                    <p>If you have any questions, please contact our HR department.</p>
                    <hr style="border: none; border-top: 1px solid #ddd;"/>
                    <p style="color: #7f8c8d; font-size: 12px;">
                        This is an automated email. Please do not reply directly.
                    </p>
                </div>
                </body>
                </html>
                """, employeeName, approvedBy);

        return sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send rejection notification email
     */
    public boolean sendRejectionEmail(String email, String employeeName, String rejectionReason) {
        String subject = "Employee Update: Record Requires Revision";
        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #e74c3c;">⚠ Record Rejected</h2>
                    <p>Hi %s,</p>
                    <p>Your employee record has been <strong style="color: #e74c3c;">REJECTED</strong> and requires revision.</p>
                    <p><strong>Reason:</strong> %s</p>
                    <p>Please contact HR to update your information.</p>
                    <hr style="border: none; border-top: 1px solid #ddd;"/>
                    <p style="color: #7f8c8d; font-size: 12px;">
                        This is an automated email. Please do not reply directly.
                    </p>
                </div>
                </body>
                </html>
                """, employeeName, rejectionReason);

        return sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send OTP email for two-factor authentication
     */
    public boolean sendOtpEmail(String email, String otp) {
        String subject = "Your One-Time Password (OTP) - Employee Management System";
        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Login Verification</h2>
                    <p>Your One-Time Password for login is:</p>
                    <div style="background-color: #ecf0f1; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px;">
                        <p style="font-size: 32px; font-weight: bold; letter-spacing: 2px; color: #2c3e50;">%s</p>
                    </div>
                    <p style="color: #e74c3c;"><strong>This code expires in 10 minutes.</strong></p>
                    <p>If you did not request this code, please ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #ddd;"/>
                    <p style="color: #7f8c8d; font-size: 12px;">
                        This is an automated email. Please do not reply directly.
                    </p>
                </div>
                </body>
                </html>
                """, otp);

        return sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String email, String resetLink) {
        String subject = "Password Reset Request - Employee Management System";
        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Password Reset Request</h2>
                    <p>We received a request to reset your password.</p>
                    <p>Click the link below to reset your password:</p>
                    <a href="%s" style="display: inline-block; background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 20px 0;">
                        Reset Password
                    </a>
                    <p style="color: #e74c3c;"><strong>This link expires in 1 hour.</strong></p>
                    <p>If you did not request a password reset, please ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #ddd;"/>
                    <p style="color: #7f8c8d; font-size: 12px;">
                        This is an automated email. Please do not reply directly.
                    </p>
                </div>
                </body>
                </html>
                """, resetLink);

        return sendHtmlEmail(email, subject, htmlContent);
    }
}
