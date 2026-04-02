package com.example.employee.dto;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for OTP setup response.
 * Contains the secret and QR code barcode URL for user to scan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpSetupResponse {

    private String secret;
    private String qrBarcode;

    public OtpSetupResponse(GoogleAuthenticatorKey key, String qrBarcode) {
        this.secret = key.getKey();
        this.qrBarcode = qrBarcode;
    }
}
