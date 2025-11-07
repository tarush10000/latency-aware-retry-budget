package com.retry.budget.util;

import com.retry.budget.config.RetryBudgetConfig;
import com.retry.budget.enums.ServiceHealthStatus;
import com.retry.budget.model.LatencyStats;
import org.springframework.stereotype.Component;

@Component
public class BudgetCalculator {
    
    private final RetryBudgetConfig config;
    
    public BudgetCalculator(RetryBudgetConfig config) {
        this.config = config;
    }
    
    public int calculateRetryBudget(LatencyStats latencyStats, double errorRate) {
        double latencyFactor = calculateLatencyFactor(latencyStats);
        double errorFactor = calculateErrorFactor(errorRate);
        
        int baseBudget = config.getBaseBudget();
        int calculatedBudget = (int) Math.round(baseBudget * latencyFactor * errorFactor);
        
        return Math.max(0, calculatedBudget);
    }
    
    public double calculateLatencyFactor(LatencyStats latencyStats) {
        if (latencyStats == null) {
            return 1.0;
        }
        
        double p95 = latencyStats.getP95();
        double healthyP95 = config.getLatencyThresholds().getHealthyP95Ms();
        double criticalP95 = config.getLatencyThresholds().getDegradedP95Ms();
        
        if (p95 <= healthyP95) {
            return 1.0;
        } else if (p95 >= criticalP95) {
            return 0.0;
        } else {
            return Math.max(0, 1.0 - (p95 - healthyP95) / (criticalP95 - healthyP95));
        }
    }
    
    public double calculateErrorFactor(double errorRate) {
        double criticalErrorRate = config.getErrorRateThresholds().getCriticalPercent();
        
        if (errorRate <= 0) {
            return 1.0;
        } else if (errorRate >= criticalErrorRate) {
            return 0.0;
        } else {
            return Math.max(0, 1.0 - (errorRate / criticalErrorRate));
        }
    }
    
    public ServiceHealthStatus determineHealthStatus(LatencyStats latencyStats, double errorRate) {
        if (latencyStats == null) {
            return ServiceHealthStatus.HEALTHY;
        }
        
        double p95 = latencyStats.getP95();
        double p99 = latencyStats.getP99();
        
        int degradedP95 = config.getLatencyThresholds().getDegradedP95Ms();
        int criticalP99 = config.getLatencyThresholds().getCriticalP99Ms();
        double criticalErrorRate = config.getErrorRateThresholds().getCriticalPercent();
        
        if (p99 > criticalP99 || errorRate > criticalErrorRate) {
            return ServiceHealthStatus.CRITICAL;
        } else if (p95 > degradedP95) {
            return ServiceHealthStatus.DEGRADED;
        } else {
            return ServiceHealthStatus.HEALTHY;
        }
    }
}