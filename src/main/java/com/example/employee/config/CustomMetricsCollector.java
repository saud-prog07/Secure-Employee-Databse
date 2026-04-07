package com.example.employee.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom metrics collector for application-specific business metrics.
 * Tracks login attempts, failed authentications, API endpoint usage, etc.
 */
@Component
@Slf4j
public class CustomMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final AtomicLong activeUsers;
    
    // Counters
    private final Counter loginAttempts;
    private final Counter loginSuccesses;
    private final Counter loginFailures;
    private final Counter registrationAttempts;
    private final Counter registrationSuccesses;
    private final Counter registrationFailures;
    private final Counter rateLimitExceeded;
    private final Counter validationErrors;
    private final Counter apiErrors;
    
    // Timers
    private final Timer loginTimer;
    private final Timer apiResponseTimer;

    public CustomMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeUsers = meterRegistry.gauge("app.active.users", new AtomicLong(0));

        // Initialize counters
        this.loginAttempts = Counter.builder("app.login.attempts")
                .description("Total login attempts")
                .register(meterRegistry);
        
        this.loginSuccesses = Counter.builder("app.login.success")
                .description("Successful logins")
                .register(meterRegistry);
        
        this.loginFailures = Counter.builder("app.login.failure")
                .description("Failed login attempts")
                .register(meterRegistry);
        
        this.registrationAttempts = Counter.builder("app.registration.attempts")
                .description("Total registration attempts")
                .register(meterRegistry);
        
        this.registrationSuccesses = Counter.builder("app.registration.success")
                .description("Successful registrations")
                .register(meterRegistry);
        
        this.registrationFailures = Counter.builder("app.registration.failure")
                .description("Failed registrations")
                .register(meterRegistry);
        
        this.rateLimitExceeded = Counter.builder("app.ratelimit.exceeded")
                .description("Rate limit exceeded count")
                .register(meterRegistry);
        
        this.validationErrors = Counter.builder("app.validation.errors")
                .description("Input validation errors")
                .register(meterRegistry);
        
        this.apiErrors = Counter.builder("app.api.errors")
                .description("API errors (5xx)")
                .register(meterRegistry);

        // Initialize timers
        this.loginTimer = Timer.builder("app.login.duration")
                .description("Login operation duration")
                .register(meterRegistry);
        
        this.apiResponseTimer = Timer.builder("app.api.response.duration")
                .description("API response time")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Record a login attempt.
     */
    public void recordLoginAttempt() {
        loginAttempts.increment();
        log.debug("Login attempt recorded");
    }

    /**
     * Record a successful login.
     */
    public void recordLoginSuccess() {
        loginSuccesses.increment();
        log.debug("Successful login recorded");
    }

    /**
     * Record a failed login.
     */
    public void recordLoginFailure() {
        loginFailures.increment();
        log.debug("Failed login recorded");
    }

    /**
     * Record login operation duration.
     */
    public Timer.Sample startLoginTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopLoginTimer(Timer.Sample sample) {
        sample.stop(loginTimer);
    }

    /**
     * Record a registration attempt.
     */
    public void recordRegistrationAttempt() {
        registrationAttempts.increment();
    }

    /**
     * Record a successful registration.
     */
    public void recordRegistrationSuccess() {
        registrationSuccesses.increment();
    }

    /**
     * Record a failed registration.
     */
    public void recordRegistrationFailure() {
        registrationFailures.increment();
    }

    /**
     * Record rate limit exceeded event.
     */
    public void recordRateLimitExceeded() {
        rateLimitExceeded.increment();
        log.warn("Rate limit exceeded");
    }

    /**
     * Record input validation error.
     */
    public void recordValidationError() {
        validationErrors.increment();
    }

    /**
     * Record API error (5xx response).
     */
    public void recordApiError() {
        apiErrors.increment();
    }

    /**
     * Record API response time.
     */
    public Timer.Sample startApiResponseTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopApiResponseTimer(Timer.Sample sample) {
        sample.stop(apiResponseTimer);
    }

    /**
     * Set number of active users.
     */
    public void setActiveUsers(long count) {
        activeUsers.set(count);
    }

    /**
     * Increment active user count.
     */
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    /**
     * Decrement active user count.
     */
    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    /**
     * Get login success rate (percentage).
     */
    public double getLoginSuccessRate() {
        double attempts = loginAttempts.count();
        if (attempts == 0) return 0;
        return (loginSuccesses.count() / attempts) * 100;
    }

    /**
     * Get registration success rate (percentage).
     */
    public double getRegistrationSuccessRate() {
        double attempts = registrationAttempts.count();
        if (attempts == 0) return 0;
        return (registrationSuccesses.count() / attempts) * 100;
    }
}
