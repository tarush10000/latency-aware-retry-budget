package com.retry.budget.service;

import com.retry.budget.config.RetryBudgetConfig;
import com.retry.budget.model.ServiceMetrics;
import com.retry.budget.repository.MetricsRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MetricsAggregatorImpl implements MetricsAggregator {
    
    private final MetricsRepository metricsRepository;
    private final MeterRegistry meterRegistry;
    private final RetryBudgetConfig config;
    
    public MetricsAggregatorImpl(MetricsRepository metricsRepository,
                                 MeterRegistry meterRegistry,
                                 RetryBudgetConfig config) {
        this.metricsRepository = metricsRepository;
        this.meterRegistry = meterRegistry;
        this.config = config;
    }
    
    @Override
    public void recordRequest(String serviceName, long latencyMs, boolean success) {
        ServiceMetrics metrics = metricsRepository.getMetrics(serviceName);
        
        if (metrics == null) {
            metrics = initializeMetrics(serviceName);
        }
        
        metrics.incrementTotalRequests();
        metrics.addLatency(latencyMs);
        
        if (success) {
            metrics.incrementSuccessfulRequests();
            meterRegistry.counter("retry.budget.requests.success", "service", serviceName).increment();
        } else {
            metrics.incrementFailedRequests();
            meterRegistry.counter("retry.budget.requests.failed", "service", serviceName).increment();
        }
        
        metrics.calculateErrorRate();
        metrics.setLastUpdated(LocalDateTime.now());
        
        metricsRepository.saveMetrics(metrics);
        
        meterRegistry.gauge("retry.budget.latency", metrics.getLatencies().stream()
                .mapToLong(Long::longValue).average().orElse(0.0));
    }
    
    @Override
    public void recordRetry(String serviceName) {
        ServiceMetrics metrics = metricsRepository.getMetrics(serviceName);
        
        if (metrics == null) {
            metrics = initializeMetrics(serviceName);
        }
        
        metrics.incrementRetryCount();
        metrics.setLastUpdated(LocalDateTime.now());
        
        metricsRepository.saveMetrics(metrics);
        
        meterRegistry.counter("retry.budget.retries", "service", serviceName).increment();
    }
    
    @Override
    public ServiceMetrics getMetrics(String serviceName) {
        ServiceMetrics metrics = metricsRepository.getMetrics(serviceName);
        
        if (metrics == null) {
            metrics = initializeMetrics(serviceName);
            metricsRepository.saveMetrics(metrics);
        }
        
        return metrics;
    }
    
    @Override
    public void resetMetrics(String serviceName) {
        ServiceMetrics metrics = initializeMetrics(serviceName);
        metricsRepository.saveMetrics(metrics);
    }
    
    private ServiceMetrics initializeMetrics(String serviceName) {
        LocalDateTime now = LocalDateTime.now();
        return ServiceMetrics.builder()
                .serviceName(serviceName)
                .totalRequests(0)
                .successfulRequests(0)
                .failedRequests(0)
                .retryCount(0)
                .errorRate(0.0)
                .lastUpdated(now)
                .windowStart(now)
                .windowEnd(now.plusSeconds(config.getEvaluationWindowSeconds()))
                .build();
    }
}