package com.retry.budget.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/api/demo")
public class DemoServiceController {

    private final Random random = new Random();
    private final AtomicInteger healthyCallCount = new AtomicInteger(0);
    private final AtomicInteger degradedCallCount = new AtomicInteger(0);
    private final AtomicInteger criticalCallCount = new AtomicInteger(0);

    /**
     * Healthy Service - Low latency (50-150ms), 95% success rate
     */
    @GetMapping("/healthy-service")
    public ServiceResponse healthyService() throws InterruptedException {
        int callNumber = healthyCallCount.incrementAndGet();
        log.info("Healthy service called - Call #{}", callNumber);

        // Simulate low latency
        int latency = 50 + random.nextInt(100);
        Thread.sleep(latency);

        // 95% success rate
        boolean success = random.nextDouble() < 0.95;

        if (!success) {
            log.warn("Healthy service failed - Call #{}", callNumber);
            throw new RuntimeException("Random failure in healthy service");
        }

        return new ServiceResponse(
            "healthy-service",
            "SUCCESS",
            latency,
            callNumber,
            "Service is healthy"
        );
    }

    /**
     * Degraded Service - Medium latency (200-400ms), 80% success rate
     */
    @GetMapping("/degraded-service")
    public ServiceResponse degradedService() throws InterruptedException {
        int callNumber = degradedCallCount.incrementAndGet();
        log.info("Degraded service called - Call #{}", callNumber);

        // Simulate medium latency
        int latency = 200 + random.nextInt(200);
        Thread.sleep(latency);

        // 80% success rate
        boolean success = random.nextDouble() < 0.80;

        if (!success) {
            log.warn("Degraded service failed - Call #{}", callNumber);
            throw new RuntimeException("Service is degraded - random failure");
        }

        return new ServiceResponse(
            "degraded-service",
            "SUCCESS",
            latency,
            callNumber,
            "Service is degraded but operational"
        );
    }

    /**
     * Critical Service - High latency (500-1000ms), 50% success rate
     */
    @GetMapping("/critical-service")
    public ServiceResponse criticalService() throws InterruptedException {
        int callNumber = criticalCallCount.incrementAndGet();
        log.info("Critical service called - Call #{}", callNumber);

        // Simulate high latency
        int latency = 500 + random.nextInt(500);
        Thread.sleep(latency);

        // 50% success rate
        boolean success = random.nextDouble() < 0.50;

        if (!success) {
            log.warn("Critical service failed - Call #{}", callNumber);
            throw new RuntimeException("Service is in critical state");
        }

        return new ServiceResponse(
            "critical-service",
            "SUCCESS",
            latency,
            callNumber,
            "Service is critical - high failure rate"
        );
    }

    /**
     * Intermittent Service - Randomly switches between healthy and unhealthy states
     */
    @GetMapping("/intermittent-service")
    public ServiceResponse intermittentService() throws InterruptedException {
        int callNumber = random.nextInt(1000);
        log.info("Intermittent service called - Call #{}", callNumber);

        // Every 10 seconds, switch behavior
        boolean isHealthy = (System.currentTimeMillis() / 10000) % 2 == 0;

        if (isHealthy) {
            int latency = 50 + random.nextInt(100);
            Thread.sleep(latency);
            return new ServiceResponse(
                "intermittent-service",
                "SUCCESS",
                latency,
                callNumber,
                "Service is currently healthy"
            );
        } else {
            int latency = 500 + random.nextInt(500);
            Thread.sleep(latency);

            if (random.nextDouble() < 0.60) {
                throw new RuntimeException("Service is currently unhealthy");
            }

            return new ServiceResponse(
                "intermittent-service",
                "SUCCESS",
                latency,
                callNumber,
                "Service is currently unhealthy but succeeded"
            );
        }
    }

    /**
     * Reset call counters
     */
    @PostMapping("/reset-counters")
    public void resetCounters() {
        healthyCallCount.set(0);
        degradedCallCount.set(0);
        criticalCallCount.set(0);
        log.info("All service counters reset");
    }

    /**
     * Get service statistics
     */
    @GetMapping("/stats")
    public ServiceStats getStats() {
        return new ServiceStats(
            healthyCallCount.get(),
            degradedCallCount.get(),
            criticalCallCount.get()
        );
    }

    // Response DTOs
    public record ServiceResponse(
        String serviceName,
        String status,
        int latencyMs,
        int callNumber,
        String message
    ) {}

    public record ServiceStats(
        int healthyCalls,
        int degradedCalls,
        int criticalCalls
    ) {}
}
