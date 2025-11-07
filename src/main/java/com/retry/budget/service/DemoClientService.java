package com.retry.budget.service;

import com.retry.budget.enums.RetryDecision;
import com.retry.budget.exception.RetryBudgetExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoClientService {

    private final BudgetEnforcementAgent budgetEnforcementAgent;
    private final MetricsAggregator metricsAggregator;
    private final RestClient restClient = RestClient.create();

    // Store recent logs for dashboard
    private final Map<String, List<RequestLog>> requestLogs = new ConcurrentHashMap<>();
    private static final int MAX_LOGS_PER_SERVICE = 100;

    /**
     * Make a request to a service with retry budget awareness
     */
    public RequestResult makeRequest(String serviceName, String endpoint) {
        long startTime = System.currentTimeMillis();
        int retryCount = 0;
        List<String> attemptLogs = new ArrayList<>();

        while (retryCount <= 3) {
            try {
                // Check if retry is allowed
                RetryDecision decision = budgetEnforcementAgent.checkRetryAllowed(serviceName);

                if (decision == RetryDecision.DENY) {
                    String msg = String.format("Retry denied for %s - budget exceeded", serviceName);
                    log.warn(msg);
                    attemptLogs.add(msg);

                    long duration = System.currentTimeMillis() - startTime;
                    logRequest(serviceName, false, duration, retryCount, "BUDGET_EXCEEDED", attemptLogs);

                    throw new RetryBudgetExceededException(serviceName, retryCount);
                }

                if (decision == RetryDecision.DEFER && retryCount > 0) {
                    String msg = String.format("Retry deferred for %s - service degraded", serviceName);
                    log.info(msg);
                    attemptLogs.add(msg);
                    Thread.sleep(100 * (retryCount + 1)); // Exponential backoff
                }

                // Make the actual request
                long requestStart = System.currentTimeMillis();
                String response = restClient.get()
                    .uri("http://localhost:8080" + endpoint)
                    .retrieve()
                    .body(String.class);

                long requestDuration = System.currentTimeMillis() - requestStart;

                // Record successful request
                metricsAggregator.recordRequest(serviceName, requestDuration, true);

                if (retryCount > 0) {
                    budgetEnforcementAgent.consumeBudget(serviceName);
                    metricsAggregator.recordRetry(serviceName);
                }

                String successMsg = String.format("Request to %s succeeded (attempt %d, latency: %dms)",
                    serviceName, retryCount + 1, requestDuration);
                log.info(successMsg);
                attemptLogs.add(successMsg);

                long totalDuration = System.currentTimeMillis() - startTime;
                logRequest(serviceName, true, totalDuration, retryCount, "SUCCESS", attemptLogs);

                return new RequestResult(true, response, retryCount, totalDuration, attemptLogs);

            } catch (Exception e) {
                long requestDuration = System.currentTimeMillis() - startTime;

                String errorMsg = String.format("Request to %s failed (attempt %d): %s",
                    serviceName, retryCount + 1, e.getMessage());
                log.error(errorMsg);
                attemptLogs.add(errorMsg);

                // Record failed request
                metricsAggregator.recordRequest(serviceName, requestDuration, false);

                if (retryCount < 3) {
                    retryCount++;
                    try {
                        Thread.sleep(50 * retryCount); // Small delay between retries
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // Max retries reached
                    long totalDuration = System.currentTimeMillis() - startTime;
                    logRequest(serviceName, false, totalDuration, retryCount, "MAX_RETRIES", attemptLogs);
                    return new RequestResult(false, null, retryCount, totalDuration, attemptLogs);
                }
            }
        }

        long totalDuration = System.currentTimeMillis() - startTime;
        logRequest(serviceName, false, totalDuration, retryCount, "FAILED", attemptLogs);
        return new RequestResult(false, null, retryCount, totalDuration, attemptLogs);
    }

    /**
     * Log request for dashboard display
     */
    private void logRequest(String serviceName, boolean success, long duration, int retries,
                           String status, List<String> logs) {
        RequestLog log = new RequestLog(
            LocalDateTime.now(),
            serviceName,
            success,
            duration,
            retries,
            status,
            new ArrayList<>(logs)
        );

        requestLogs.computeIfAbsent(serviceName, k -> new ArrayList<>());
        List<RequestLog> logs_list = requestLogs.get(serviceName);

        synchronized (logs_list) {
            logs_list.add(0, log); // Add to beginning
            if (logs_list.size() > MAX_LOGS_PER_SERVICE) {
                logs_list.remove(logs_list.size() - 1); // Remove oldest
            }
        }
    }

    /**
     * Get recent logs for a service
     */
    public List<RequestLog> getRecentLogs(String serviceName) {
        return requestLogs.getOrDefault(serviceName, new ArrayList<>());
    }

    /**
     * Get all recent logs
     */
    public Map<String, List<RequestLog>> getAllRecentLogs() {
        return new ConcurrentHashMap<>(requestLogs);
    }

    /**
     * Clear logs
     */
    public void clearLogs() {
        requestLogs.clear();
    }

    // DTOs
    public record RequestResult(
        boolean success,
        String response,
        int retryCount,
        long totalDuration,
        List<String> attemptLogs
    ) {}

    public record RequestLog(
        LocalDateTime timestamp,
        String serviceName,
        boolean success,
        long duration,
        int retries,
        String status,
        List<String> logs
    ) {}
}
