package com.retry.budget.controller;

import com.retry.budget.model.RetryBudget;
import com.retry.budget.model.ServiceMetrics;
import com.retry.budget.service.BudgetEnforcementAgent;
import com.retry.budget.service.DemoClientService;
import com.retry.budget.service.MetricsAggregator;
import com.retry.budget.service.RetryBudgetControllerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoClientService demoClientService;
    private final RetryBudgetControllerService retryBudgetService;
    private final MetricsAggregator metricsAggregator;
    private final BudgetEnforcementAgent budgetEnforcementAgent;

    /**
     * Trigger a request to a specific service
     */
    @PostMapping("/trigger/{serviceName}")
    public ResponseEntity<DemoClientService.RequestResult> triggerRequest(@PathVariable String serviceName) {
        String endpoint = switch (serviceName) {
            case "healthy-service" -> "/api/demo/healthy-service";
            case "degraded-service" -> "/api/demo/degraded-service";
            case "critical-service" -> "/api/demo/critical-service";
            case "intermittent-service" -> "/api/demo/intermittent-service";
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };

        DemoClientService.RequestResult result = demoClientService.makeRequest(serviceName, endpoint);
        return ResponseEntity.ok(result);
    }

    /**
     * Trigger multiple requests to all services
     */
    @PostMapping("/trigger-all")
    @Async
    public CompletableFuture<Map<String, DemoClientService.RequestResult>> triggerAllServices() {
        Map<String, DemoClientService.RequestResult> results = new HashMap<>();

        String[] services = {"healthy-service", "degraded-service", "critical-service", "intermittent-service"};

        for (String service : services) {
            String endpoint = "/api/demo/" + service;
            DemoClientService.RequestResult result = demoClientService.makeRequest(service, endpoint);
            results.put(service, result);
        }

        return CompletableFuture.completedFuture(results);
    }

    /**
     * Start continuous load testing
     */
    @PostMapping("/start-load")
    @Async
    public CompletableFuture<String> startLoad(@RequestParam(defaultValue = "10") int durationSeconds) {
        log.info("Starting load test for {} seconds", durationSeconds);

        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        String[] services = {"healthy-service", "degraded-service", "critical-service", "intermittent-service"};

        while (System.currentTimeMillis() < endTime) {
            for (String service : services) {
                String endpoint = "/api/demo/" + service;
                demoClientService.makeRequest(service, endpoint);

                try {
                    Thread.sleep(500); // 500ms between requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return CompletableFuture.completedFuture("Load test interrupted");
                }
            }
        }

        return CompletableFuture.completedFuture("Load test completed");
    }

    /**
     * Get dashboard data (all metrics, budgets, and logs)
     */
    @GetMapping("/dashboard-data")
    public ResponseEntity<DashboardData> getDashboardData() {
        Map<String, ServiceStatus> serviceStatuses = new HashMap<>();

        String[] services = {"healthy-service", "degraded-service", "critical-service", "intermittent-service"};

        for (String service : services) {
            ServiceMetrics metrics = metricsAggregator.getMetrics(service);
            RetryBudget budget = retryBudgetService.getBudget(service);

            ServiceStatus status = new ServiceStatus(
                service,
                metrics,
                budget,
                demoClientService.getRecentLogs(service)
            );

            serviceStatuses.put(service, status);
        }

        return ResponseEntity.ok(new DashboardData(serviceStatuses));
    }

    /**
     * Get logs for a specific service
     */
    @GetMapping("/logs/{serviceName}")
    public ResponseEntity<List<DemoClientService.RequestLog>> getLogs(@PathVariable String serviceName) {
        return ResponseEntity.ok(demoClientService.getRecentLogs(serviceName));
    }

    /**
     * Clear all logs
     */
    @PostMapping("/clear-logs")
    public ResponseEntity<String> clearLogs() {
        demoClientService.clearLogs();
        return ResponseEntity.ok("Logs cleared");
    }

    /**
     * Reset all demo data
     */
    @PostMapping("/reset-all")
    public ResponseEntity<String> resetAll() {
        demoClientService.clearLogs();

        String[] services = {"healthy-service", "degraded-service", "critical-service", "intermittent-service"};
        for (String service : services) {
            metricsAggregator.resetMetrics(service);
        }

        return ResponseEntity.ok("All demo data reset");
    }

    // DTOs
    public record DashboardData(
        Map<String, ServiceStatus> services
    ) {}

    public record ServiceStatus(
        String name,
        ServiceMetrics metrics,
        RetryBudget budget,
        List<DemoClientService.RequestLog> recentLogs
    ) {}
}
