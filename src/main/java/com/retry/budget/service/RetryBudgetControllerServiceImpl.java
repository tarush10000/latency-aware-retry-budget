package com.retry.budget.service;

import com.retry.budget.config.RetryBudgetConfig;
import com.retry.budget.model.LatencyStats;
import com.retry.budget.model.RetryBudget;
import com.retry.budget.model.ServiceMetrics;
import com.retry.budget.repository.RetryBudgetRepository;
import com.retry.budget.util.BudgetCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class RetryBudgetControllerServiceImpl implements RetryBudgetControllerService {
    
    private final MetricsAggregator metricsAggregator;
    private final LatencyAnalyzer latencyAnalyzer;
    private final BudgetCalculator budgetCalculator;
    private final RetryBudgetRepository budgetRepository;
    private final RetryBudgetConfig config;
    
    public RetryBudgetControllerServiceImpl(MetricsAggregator metricsAggregator,
                                            LatencyAnalyzer latencyAnalyzer,
                                            BudgetCalculator budgetCalculator,
                                            RetryBudgetRepository budgetRepository,
                                            RetryBudgetConfig config) {
        this.metricsAggregator = metricsAggregator;
        this.latencyAnalyzer = latencyAnalyzer;
        this.budgetCalculator = budgetCalculator;
        this.budgetRepository = budgetRepository;
        this.config = config;
    }
    
    @Override
    public RetryBudget calculateBudget(String serviceName) {
        ServiceMetrics metrics = metricsAggregator.getMetrics(serviceName);
        LatencyStats latencyStats = latencyAnalyzer.analyzeLatency(metrics);
        
        double errorRate = metrics.getErrorRate();
        int calculatedBudget = budgetCalculator.calculateRetryBudget(latencyStats, errorRate);
        
        double latencyFactor = budgetCalculator.calculateLatencyFactor(latencyStats);
        double errorFactor = budgetCalculator.calculateErrorFactor(errorRate);
        
        RetryBudget budget = RetryBudget.builder()
                .serviceName(serviceName)
                .allocatedBudget(calculatedBudget)
                .usedBudget(0)
                .remainingBudget(calculatedBudget)
                .healthStatus(budgetCalculator.determineHealthStatus(latencyStats, errorRate))
                .latencyFactor(latencyFactor)
                .errorFactor(errorFactor)
                .lastCalculated(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(config.getUpdateIntervalSeconds()))
                .build();
        
        budgetRepository.saveBudget(budget);
        
        return budget;
    }
    
    @Override
    public RetryBudget getBudget(String serviceName) {
        RetryBudget budget = budgetRepository.getBudget(serviceName);
        
        if (budget == null || LocalDateTime.now().isAfter(budget.getExpiresAt())) {
            budget = calculateBudget(serviceName);
        }
        
        return budget;
    }
    
    @Override
    public void updateAllBudgets() {
        Set<String> serviceNames = budgetRepository.getAllServiceNames();
        
        for (String serviceName : serviceNames) {
            calculateBudget(serviceName);
        }
    }
}