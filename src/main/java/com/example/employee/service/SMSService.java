package com.example.employee.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SMSService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    /**
     * Send SMS message to a phone number
     */
    public boolean sendSMS(String toPhoneNumber, String messageBody) {
        try {
            // Initialize Twilio
            Twilio.init(accountSid, authToken);

            // Send message
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),      // To number
                    new PhoneNumber(twilioPhoneNumber),  // From number
                    messageBody                          // Message body
            ).create();

            log.info("SMS sent successfully to: {}, MessageSid: {}", toPhoneNumber, message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", toPhoneNumber, e);
            return false;
        }
    }

    /**
     * Send OTP via SMS
     */
    public boolean sendOtpSMS(String phoneNumber, String otp) {
        String message = String.format("Your OTP for Employee Management System is: %s. This code expires in 10 minutes. Do not share this code with anyone.", otp);
        return sendSMS(phoneNumber, message);
    }

    /**
     * Send login notification via SMS
     */
    public boolean sendLoginNotificationSMS(String phoneNumber, String username) {
        String message = String.format("Login notification: Someone logged into your account (%s). If this wasn't you, please secure your account immediately.", username);
        return sendSMS(phoneNumber, message);
    }

    /**
     * Send approval notification via SMS
     */
    public boolean sendApprovalSMS(String phoneNumber, String employeeName) {
        String message = String.format("Great news %s! Your employee record has been approved. You can now access all system features.", employeeName);
        return sendSMS(phoneNumber, message);
    }

    /**
     * Send rejection notification via SMS
     */
    public boolean sendRejectionSMS(String phoneNumber, String employeeName) {
        String message = String.format("Update on your record, %s: Your employee record requires revision. Please contact HR for more details.", employeeName);
        return sendSMS(phoneNumber, message);
    }
}
