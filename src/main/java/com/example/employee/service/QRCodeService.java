package com.example.employee.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class QRCodeService {

    private static final int QR_CODE_SIZE = 300;
    private static final String EMPLOYEE_PREFIX = "EMP-";

    public byte[] generateQRCode(Long employeeId) {
        try {
            String qrContent = EMPLOYEE_PREFIX + employeeId;
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            log.info("QR code generated successfully for employee ID: {}", employeeId);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for employee ID: {}", employeeId, e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }

    public String generateQRCodeDataURL(Long employeeId) {
        try {
            byte[] qrCodeImage = generateQRCode(employeeId);
            String base64Image = java.util.Base64.getEncoder().encodeToString(qrCodeImage);
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            log.error("Error generating QR code data URL for employee ID: {}", employeeId, e);
            throw new RuntimeException("Failed to generate QR code data URL: " + e.getMessage());
        }
    }
}
