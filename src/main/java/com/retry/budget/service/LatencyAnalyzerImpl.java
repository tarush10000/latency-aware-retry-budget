package com.retry.budget.service;

import com.retry.budget.model.LatencyStats;
import com.retry.budget.model.ServiceMetrics;
import com.retry.budget.repository.MetricsRepository;
import com.retry.budget.util.PercentileCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LatencyAnalyzerImpl implements LatencyAnalyzer {
    
    private final PercentileCalculator percentileCalculator;
    private final MetricsRepository metricsRepository;
    
    public LatencyAnalyzerImpl(PercentileCalculator percentileCalculator,
                               MetricsRepository metricsRepository) {
        this.percentileCalculator = percentileCalculator;
        this.metricsRepository = metricsRepository;
    }
    
    @Override
    public LatencyStats analyzeLatency(ServiceMetrics metrics) {
        if (metrics == null || metrics.getLatencies().isEmpty()) {
            return LatencyStats.builder()
                    .p50(0)
                    .p95(0)
                    .p99(0)
                    .mean(0)
                    .standardDeviation(0)
                    .sampleSize(0)
                    .build();
        }
        
        List<Long> latencies = metrics.getLatencies();
        
        return LatencyStats.builder()
                .p50(percentileCalculator.calculatePercentile(latencies, 50))
                .p95(percentileCalculator.calculatePercentile(latencies, 95))
                .p99(percentileCalculator.calculatePercentile(latencies, 99))
                .mean(percentileCalculator.calculateMean(latencies))
                .standardDeviation(percentileCalculator.calculateStandardDeviation(latencies))
                .min(latencies.stream().min(Long::compareTo).orElse(0L))
                .max(latencies.stream().max(Long::compareTo).orElse(0L))
                .sampleSize(latencies.size())
                .build();
    }
    
    @Override
    public LatencyStats analyzeLatency(String serviceName) {
        ServiceMetrics metrics = metricsRepository.getMetrics(serviceName);
        return analyzeLatency(metrics);
    }
}