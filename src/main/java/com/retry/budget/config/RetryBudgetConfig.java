package com.retry.budget.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "retry.budget")
public class RetryBudgetConfig {
    
    private int baseBudget = 5;
    private int evaluationWindowSeconds = 60;
    private int updateIntervalSeconds = 10;
    private LatencyThresholds latencyThresholds = new LatencyThresholds();
    private ErrorRateThresholds errorRateThresholds = new ErrorRateThresholds();
    
    @Data
    public static class LatencyThresholds {
        private int healthyP95Ms = 200;
        private int degradedP95Ms = 300;
        private int criticalP99Ms = 800;
    }
    
    @Data
    public static class ErrorRateThresholds {
        private double warningPercent = 5.0;
        private double criticalPercent = 10.0;
    }
}