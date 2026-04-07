package com.example.employee.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Logging filter for comprehensive HTTP request/response monitoring.
 * Logs all API calls with request details, response status, and execution time.
 */
@Component
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    /**
     * Filters HTTP requests and logs request/response details.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip logging for non-API endpoints
        String requestUri = request.getRequestURI();
        if (shouldSkip(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate correlation ID for request tracing
        String correlationId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logRequest(request, correlationId);

            // Process request
            filterChain.doFilter(request, response);

        } finally {
            // Log response
            long duration = System.currentTimeMillis() - startTime;
            logResponse(request, response, duration, correlationId);
        }
    }

    /**
     * Logs incoming HTTP request details.
     */
    private void logRequest(HttpServletRequest request, String correlationId) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[REQUEST] ")
                .append("CorrelationId=").append(correlationId).append(" | ")
                .append("Method=").append(request.getMethod()).append(" | ")
                .append("URI=").append(request.getRequestURI()).append(" | ")
                .append("RemoteAddr=").append(getClientIpAddress(request)).append(" | ")
                .append("ContentType=").append(request.getContentType()).append(" | ")
                .append("Principal=").append(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous");

        log.info(logMessage.toString());
    }

    /**
     * Logs HTTP response details and execution time.
     */
    private void logResponse(HttpServletRequest request, HttpServletResponse response, long duration, String correlationId) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[RESPONSE] ")
                .append("CorrelationId=").append(correlationId).append(" | ")
                .append("Method=").append(request.getMethod()).append(" | ")
                .append("URI=").append(request.getRequestURI()).append(" | ")
                .append("Status=").append(response.getStatus()).append(" | ")
                .append("Duration=").append(duration).append("ms | ")
                .append("ContentType=").append(response.getContentType());

        // Log appropriate level based on status code
        int status = response.getStatus();
        if (status >= 500) {
            log.error(logMessage.toString());
        } else if (status >= 400) {
            log.warn(logMessage.toString());
        } else {
            log.info(logMessage.toString());
        }
    }

    /**
     * Determines if request should be skipped from detailed logging.
     */
    private boolean shouldSkip(String uri) {
        return uri.contains("/actuator") ||
               uri.contains("/swagger-ui") ||
               uri.contains("/v3/api-docs") ||
               uri.contains("/health") ||
               uri.contains("/metrics") ||
               uri.equals("/") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".gif") ||
               uri.endsWith(".ico");
    }

    /**
     * Extracts client IP address from request, handling proxies.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }
}
