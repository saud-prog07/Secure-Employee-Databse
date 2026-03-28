package com.example.employee.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Deprecated: JWT authentication has been removed from this project.
 * This class is kept as a stub to prevent breaking references.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Deprecated(forRemoval = true)
    public String generateJwtToken(Authentication authentication) {
        throw new UnsupportedOperationException("JWT authentication is no longer supported");
    }

    @Deprecated(forRemoval = true)
    public String getUserNameFromJwtToken(String token) {
        throw new UnsupportedOperationException("JWT authentication is no longer supported");
    }

    @Deprecated(forRemoval = true)
    public boolean validateJwtToken(String authToken) {
        throw new UnsupportedOperationException("JWT authentication is no longer supported");
    }
}
