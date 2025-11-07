package com.retry.budget.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetrics {
    
    private String serviceName;
    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private long retryCount;
    private double errorRate;
    
    @Builder.Default
    private List<Long> latencies = new ArrayList<>();
    
    private LatencyStats latencyStats;
    
    private LocalDateTime lastUpdated;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    
    public void addLatency(long latencyMs) {
        if (this.latencies == null) {
            this.latencies = new ArrayList<>();
        }
        this.latencies.add(latencyMs);
        
        if (this.latencies.size() > 1000) {
            this.latencies = new ArrayList<>(this.latencies.subList(this.latencies.size() - 1000, this.latencies.size()));
        }
    }
    
    public void incrementTotalRequests() {
        this.totalRequests++;
    }
    
    public void incrementSuccessfulRequests() {
        this.successfulRequests++;
    }
    
    public void incrementFailedRequests() {
        this.failedRequests++;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public void calculateErrorRate() {
        if (totalRequests > 0) {
            this.errorRate = (double) failedRequests / totalRequests * 100.0;
        } else {
            this.errorRate = 0.0;
        }
    }
}