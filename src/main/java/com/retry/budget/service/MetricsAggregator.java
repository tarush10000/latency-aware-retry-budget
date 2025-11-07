package com.retry.budget.service;

import com.retry.budget.model.ServiceMetrics;

public interface MetricsAggregator {
    
    void recordRequest(String serviceName, long latencyMs, boolean success);
    
    void recordRetry(String serviceName);
    
    ServiceMetrics getMetrics(String serviceName);
    
    void resetMetrics(String serviceName);
}