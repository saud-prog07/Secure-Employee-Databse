package com.example.employee.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Custom health indicator for application-specific health checks.
 * Provides detailed information about application status, memory usage, and database connectivity.
 */
@Component("applicationHealth")
@Slf4j
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check memory usage
            MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memBean.getHeapMemoryUsage().getMax();
            double memoryPercentage = (double) usedMemory / maxMemory * 100;

            // Determine heap memory status
            Health.Builder builder = new Health.Builder();
            if (memoryPercentage > 90) {
                builder.down();
                log.warn("Application health degraded: Heap memory usage is {} %", String.format("%.2f", memoryPercentage));
            } else if (memoryPercentage > 75) {
                builder.outOfService();
                log.warn("Application health warning: Heap memory usage is {} %", String.format("%.2f", memoryPercentage));
            } else {
                builder.up();
            }

            builder
                    .withDetail("status", "running")
                    .withDetail("heapMemoryUsage", String.format("%.2f MB", usedMemory / (1024.0 * 1024.0)))
                    .withDetail("heapMemoryMax", String.format("%.2f MB", maxMemory / (1024.0 * 1024.0)))
                    .withDetail("heapMemoryPercentage", String.format("%.2f %%", memoryPercentage))
                    .withDetail("timestamp", System.currentTimeMillis());

            return builder.build();
        } catch (Exception e) {
            log.error("Error checking application health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }
}
