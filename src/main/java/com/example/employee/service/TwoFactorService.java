package com.example.employee.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.example.employee.entity.User;
import com.example.employee.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing Two-Factor Authentication (2FA) using TOTP (Time-based One-Time Password).
 * Uses Google Authenticator compatible algorithm.
 */
@Service
@Slf4j
public class TwoFactorService {

    private final GoogleAuthenticator googleAuthenticator;
    private final UserRepository userRepository;

    public TwoFactorService(UserRepository userRepository) {
        this.googleAuthenticator = new GoogleAuthenticator();
        this.userRepository = userRepository;
    }

    /**
     * Generate a new 2FA secret for a user.
     *
     * @return GoogleAuthenticatorKey containing the secret and QR barcode URL
     */
    public GoogleAuthenticatorKey generateSecret() {
        return googleAuthenticator.createCredentials();
    }

    /**
     * Enable 2FA for a user by saving the secret.
     *
     * @param user The user entity
     * @param secret The 2FA secret key
     */
    public void enableTwoFactorAuth(User user, String secret) {
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);
        userRepository.save(user);
        log.info("2FA enabled for user: {}", user.getUsername());
    }

    /**
     * Disable 2FA for a user.
     *
     * @param user The user entity
     */
    public void disableTwoFactorAuth(User user) {
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        log.info("2FA disabled for user: {}", user.getUsername());
    }

    /**
     * Verify an OTP against the user's 2FA secret.
     *
     * @param secret The user's 2FA secret
     * @param otp The OTP code provided by the user
     * @return true if OTP is valid, false otherwise
     */
    public boolean verifyOtp(String secret, int otp) {
        try {
            boolean isValid = googleAuthenticator.authorize(secret, otp);
            if (isValid) {
                log.debug("OTP verification successful");
            } else {
                log.warn("OTP verification failed - invalid code");
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get QR code barcode URL for a user to scan with authenticator app.
     * Format: otpauth://totp/issuer:username?secret=KEY&issuer=ISSUER
     *
     * @param key The GoogleAuthenticatorKey containing credentials
     * @param username The username for display in authenticator app
     * @param issuer The issuer name (application name)
     * @return OTPAuth URI format for QR code generation
     */
    public String getQrBarcodeUrl(GoogleAuthenticatorKey key, String username, String issuer) {
        String secret = key.getKey();
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", 
                issuer, username, secret, issuer);
    }

    /**
     * Check if a user has 2FA enabled.
     *
     * @param user The user entity
     * @return true if 2FA is enabled, false otherwise
     */
    public boolean isTwoFactorEnabled(User user) {
        return user != null && user.isTwoFactorEnabled();
    }
}
