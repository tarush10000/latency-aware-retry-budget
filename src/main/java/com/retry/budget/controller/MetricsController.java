package com.retry.budget.controller;

import com.retry.budget.model.LatencyStats;
import com.retry.budget.model.ServiceMetrics;
import com.retry.budget.service.LatencyAnalyzer;
import com.retry.budget.service.MetricsAggregator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final MetricsAggregator metricsAggregator;
    private final LatencyAnalyzer latencyAnalyzer;
    
    public MetricsController(MetricsAggregator metricsAggregator,
                            LatencyAnalyzer latencyAnalyzer) {
        this.metricsAggregator = metricsAggregator;
        this.latencyAnalyzer = latencyAnalyzer;
    }
    
    @GetMapping("/{serviceName}")
    public ResponseEntity<ServiceMetrics> getMetrics(@PathVariable String serviceName) {
        ServiceMetrics metrics = metricsAggregator.getMetrics(serviceName);
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping("/record")
    public ResponseEntity<Map<String, String>> recordRequest(
            @RequestParam String serviceName,
            @RequestParam long latencyMs,
            @RequestParam boolean success) {
        
        metricsAggregator.recordRequest(serviceName, latencyMs, success);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Request recorded successfully");
        response.put("serviceName", serviceName);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/retry/{serviceName}")
    public ResponseEntity<Map<String, String>> recordRetry(@PathVariable String serviceName) {
        metricsAggregator.recordRetry(serviceName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Retry recorded for service: " + serviceName);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/latency/{serviceName}")
    public ResponseEntity<LatencyStats> getLatencyStats(@PathVariable String serviceName) {
        LatencyStats stats = latencyAnalyzer.analyzeLatency(serviceName);
        return ResponseEntity.ok(stats);
    }
    
    @DeleteMapping("/{serviceName}")
    public ResponseEntity<Map<String, String>> resetMetrics(@PathVariable String serviceName) {
        metricsAggregator.resetMetrics(serviceName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Metrics reset for service: " + serviceName);
        
        return ResponseEntity.ok(response);
    }
}