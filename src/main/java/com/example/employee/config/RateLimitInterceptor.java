package com.example.employee.config;

import com.example.employee.util.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP interceptor for enforcing rate limits on API requests.
 * Uses the RateLimiter utility to track and control request frequency per user/IP.
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // Extract identifier (use username if authenticated, otherwise use IP address)
        String identifier = getIdentifier(request);

        // Apply stricter limits to authentication endpoints
        String requestUri = request.getRequestURI();
        boolean allowed;

        if (isAuthEndpoint(requestUri)) {
            allowed = rateLimiter.allowAuthRequest(identifier);
            if (!allowed) {
                log.warn("Rate limit exceeded for auth endpoint - Identifier: {}, URI: {}", identifier, requestUri);
                sendRateLimitError(response, 429, "Too many authentication attempts. Please try again later.");
                return false;
            }
        } else if (isApiEndpoint(requestUri)) {
            allowed = rateLimiter.allowApiRequest(identifier);
            if (!allowed) {
                log.warn("Rate limit exceeded for API endpoint - Identifier: {}, URI: {}", identifier, requestUri);
                sendRateLimitError(response, 429, "Rate limit exceeded. Too many requests.");
                return false;
            }
        }

        // Add rate limit information to response headers
        long remainingRequests = rateLimiter.getRemainingRequests(identifier);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remainingRequests)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000)); // Reset in 1 minute

        return true;
    }

    /**
     * Extracts the identifier (username or IP address) from the request.
     *
     * @param request The HTTP request
     * @return The identifier string
     */
    private String getIdentifier(HttpServletRequest request) {
        // Try to get username from security context
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;

        if (username != null && !username.isEmpty()) {
            return "user:" + username;
        }

        // Fall back to IP address
        String clientIp = getClientIpAddress(request);
        return "ip:" + clientIp;
    }

    /**
     * Extracts the client IP address from the request.
     * Handles proxies and X-Forwarded-For headers.
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check for X-Forwarded-For header (used by proxies)
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return forwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Use the remote address as fallback
        return request.getRemoteAddr();
    }

    /**
     * Determines if the request URI is an authentication endpoint.
     *
     * @param uri The request URI
     * @return true if it's an auth endpoint
     */
    private boolean isAuthEndpoint(String uri) {
        return uri.contains("/api/auth/login") || 
               uri.contains("/api/auth/register") ||
               uri.contains("/api/auth/forgot-password") ||
               uri.contains("/api/auth/reset-password") ||
               uri.contains("/api/auth/verify-otp");
    }

    /**
     * Determines if the request URI is an API endpoint (not static content).
     *
     * @param uri The request URI
     * @return true if it's an API endpoint
     */
    private boolean isApiEndpoint(String uri) {
        return uri.startsWith("/api/") && !uri.contains("/swagger") && !uri.contains("/docs");
    }

    /**
     * Sends a rate limit error response.
     *
     * @param response The HTTP response
     * @param statusCode The HTTP status code
     * @param message The error message
     * @throws IOException If an I/O error occurs
     */
    private void sendRateLimitError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setHeader("Retry-After", "60"); // Retry after 60 seconds

        String jsonResponse = String.format(
                "{\"status\":\"error\",\"message\":\"%s\",\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
