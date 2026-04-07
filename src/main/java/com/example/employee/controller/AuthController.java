package com.example.employee.controller;

import com.example.employee.dto.*;
import com.example.employee.entity.User;
import com.example.employee.repository.UserRepository;
import com.example.employee.service.TwoFactorService;
import com.example.employee.service.NotificationService;
import com.example.employee.service.AuditLogService;
import com.example.employee.security.JwtUtils;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final JwtUtils jwtUtils;
    private final TwoFactorService twoFactorService;
    private final NotificationService notificationService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          AuthenticationManager authenticationManager,
                          AuditLogService auditLogService,
                          JwtUtils jwtUtils,
                          TwoFactorService twoFactorService,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
        this.jwtUtils = jwtUtils;
        this.twoFactorService = twoFactorService;
        this.notificationService = notificationService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with BCrypt encrypted password.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Process: Registration. Target User: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: Username '{}' is already taken", request.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Username already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : com.example.employee.entity.Role.HR);
        user.setApproved(false);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);
        auditLogService.logAction("REGISTER_HR", user.getUsername());
        notificationService.sendRegistrationNotification(user.getUsername(), user.getUsername());
        savedUser.setPassword("********");

        log.info("Registration successful for user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Confirmation email sent.", savedUser));
    }

    @Operation(summary = "Login a user", description = "Authenticates a user with username and password.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Process: Login. Attempting user: {}", request.getIdentifier());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier())
                    .orElseThrow(() -> new Exception("User not found"));

            if (twoFactorService.isTwoFactorEnabled(user)) {
                log.info("2FA is enabled for user: {}", request.getIdentifier());
                AuthResponse response = new AuthResponse(request.getIdentifier(), true);
                auditLogService.logAction("LOGIN_AWAITING_OTP", request.getIdentifier());
                return ResponseEntity.ok(ApiResponse.success("OTP required", response));
            }

            String jwt = jwtUtils.generateJwtToken(authentication);
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            auditLogService.logAction("LOGIN", request.getIdentifier());
            AuthResponse authResponse = new AuthResponse(jwt, request.getIdentifier(), roles);
            log.info("Login successful for user: {}", request.getIdentifier());
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (Exception e) {
            log.warn("Login failed for user '{}': {}", request.getIdentifier(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @Operation(summary = "Verify OTP", description = "Verifies the OTP code for 2FA.")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@Valid @RequestBody OtpRequest otpRequest) {
        log.info("Process: OTP Verification. User: {}", otpRequest.getUsername());

        try {
            User user = userRepository.findByUsername(otpRequest.getUsername())
                    .orElseThrow(() -> new Exception("User not found"));

            if (!twoFactorService.isTwoFactorEnabled(user)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("2FA is not enabled for this user"));
            }

            int otpCode = Integer.parseInt(otpRequest.getOtp());
            boolean isValid = twoFactorService.verifyOtp(user.getTwoFactorSecret(), otpCode);

            if (!isValid) {
                auditLogService.logAction("OTP_VERIFICATION_FAILED", otpRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid OTP code"));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            String jwt = jwtUtils.generateJwtToken(authentication);
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            auditLogService.logAction("LOGIN_2FA_VERIFIED", otpRequest.getUsername());
            AuthResponse authResponse = new AuthResponse(jwt, otpRequest.getUsername(), roles);
            return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", authResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error verifying OTP"));
        }
    }

    @Operation(summary = "Forgot Password", description = "Generates a reset token and sets a 15-minute expiry.")
    @PostMapping("/forgot-password")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Process: Forgot Password. Target Email: {}", request.getEmail());
        
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            log.info("Reset token generated for user {}: {}", user.getUsername(), token);
        });

        return ResponseEntity.ok(ApiResponse.success("If an account with that email exists, a reset link has been sent.", null));
    }

    @Operation(summary = "Reset Password", description = "Validates the reset token and updates the user's password.")
    @PostMapping("/reset-password")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Process: Reset Password. Token: {}", request.getToken());

        return userRepository.findByResetToken(request.getToken())
                .map(user -> {
                    if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                        log.warn("Reset attempt failed: Token expired for user {}", user.getUsername());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Reset token has expired"));
                    }

                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    user.setResetToken(null);
                    user.setResetTokenExpiry(null);
                    userRepository.save(user);

                    auditLogService.logAction("PASSWORD_RESET", user.getUsername());
                    log.info("Password successfully reset for user: {}", user.getUsername());
                    return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
                })
                .orElseGet(() -> {
                    log.warn("Reset attempt failed: Invalid token provided");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("Invalid reset token"));
                });
    }

    @Operation(summary = "Setup 2FA", description = "Returns QR code for 2FA scanning.")
    @PostMapping("/2fa/setup")
    public ResponseEntity<ApiResponse<OtpSetupResponse>> setup2Fa(@RequestHeader("Authorization") String bearerToken) {
        try {
            String username = jwtUtils.getUserNameFromJwtToken(bearerToken.substring(7));
            userRepository.findByUsername(username).orElseThrow(() -> new Exception("User not found"));
            GoogleAuthenticatorKey key = twoFactorService.generateSecret();
            String qrBarcode = twoFactorService.getQrBarcodeUrl(key, username, "Employee Management System");
            OtpSetupResponse response = new OtpSetupResponse(key, qrBarcode);
            return ResponseEntity.ok(ApiResponse.success("2FA setup initiated", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error setting up 2FA"));
        }
    }

    @Operation(summary = "Disable 2FA", description = "Disables 2FA for the authenticated user.")
    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<Object>> disable2Fa(@RequestHeader("Authorization") String bearerToken) {
        try {
            String username = jwtUtils.getUserNameFromJwtToken(bearerToken.substring(7));
            User user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("User not found"));
            twoFactorService.disableTwoFactorAuth(user);
            auditLogService.logAction("2FA_DISABLED", username);
            return ResponseEntity.ok(ApiResponse.success("2FA disabled successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error disabling 2FA"));
        }
    }
}
