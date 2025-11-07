package com.retry.budget.service;

import com.retry.budget.model.LatencyStats;
import com.retry.budget.model.ServiceMetrics;

public interface LatencyAnalyzer {
    
    LatencyStats analyzeLatency(ServiceMetrics metrics);
    
    LatencyStats analyzeLatency(String serviceName);
}