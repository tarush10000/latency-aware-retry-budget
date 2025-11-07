package com.retry.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsAggregator metricsAggregator;
    private final RetryBudgetControllerService retryBudgetService;

    /**
     * Broadcast dashboard updates every second
     */
    @Scheduled(fixedRate = 1000)
    public void broadcastDashboardUpdates() {
        try {
            Map<String, Object> update = new HashMap<>();

            String[] services = {"healthy-service", "degraded-service", "critical-service", "intermittent-service"};

            for (String service : services) {
                Map<String, Object> serviceData = new HashMap<>();

                var metrics = metricsAggregator.getMetrics(service);
                var budget = retryBudgetService.getBudget(service);

                serviceData.put("metrics", metrics);
                serviceData.put("budget", budget);

                update.put(service, serviceData);
            }

            update.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/dashboard", update);

        } catch (Exception e) {
            log.error("Error broadcasting dashboard updates", e);
        }
    }

    /**
     * Broadcast a specific event
     */
    public void broadcastEvent(String eventType, Object data) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", eventType);
        event.put("data", data);
        event.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/events", event);
    }
}
