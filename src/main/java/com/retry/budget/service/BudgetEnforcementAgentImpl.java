package com.retry.budget.service;

import com.retry.budget.enums.RetryDecision;
import com.retry.budget.enums.ServiceHealthStatus;
import com.retry.budget.model.RetryBudget;
import com.retry.budget.repository.RetryBudgetRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class BudgetEnforcementAgentImpl implements BudgetEnforcementAgent {
    
    private final RetryBudgetControllerService budgetControllerService;
    private final RetryBudgetRepository budgetRepository;
    private final MeterRegistry meterRegistry;
    
    public BudgetEnforcementAgentImpl(RetryBudgetControllerService budgetControllerService,
                                      RetryBudgetRepository budgetRepository,
                                      MeterRegistry meterRegistry) {
        this.budgetControllerService = budgetControllerService;
        this.budgetRepository = budgetRepository;
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    public RetryDecision checkRetryAllowed(String serviceName) {
        RetryBudget budget = budgetControllerService.getBudget(serviceName);
        
        if (budget.getHealthStatus() == ServiceHealthStatus.CRITICAL) {
            meterRegistry.counter("retry.budget.decision.deny.critical", "service", serviceName).increment();
            return RetryDecision.DENY;
        }
        
        if (budget.getRemainingBudget() <= 0) {
            meterRegistry.counter("retry.budget.decision.deny.exceeded", "service", serviceName).increment();
            return RetryDecision.DENY;
        }
        
        if (budget.getHealthStatus() == ServiceHealthStatus.DEGRADED) {
            meterRegistry.counter("retry.budget.decision.defer", "service", serviceName).increment();
            return RetryDecision.DEFER;
        }
        
        meterRegistry.counter("retry.budget.decision.allow", "service", serviceName).increment();
        return RetryDecision.ALLOW;
    }
    
    @Override
    public void consumeBudget(String serviceName) {
        RetryBudget budget = budgetControllerService.getBudget(serviceName);
        budget.consumeBudget();
        budgetRepository.saveBudget(budget);
        
        meterRegistry.counter("retry.budget.consumed", "service", serviceName).increment();
        meterRegistry.gauge("retry.budget.remaining", budget.getRemainingBudget());
    }
    
    @Override
    public boolean isServiceHealthy(String serviceName) {
        RetryBudget budget = budgetControllerService.getBudget(serviceName);
        return budget.getHealthStatus() == ServiceHealthStatus.HEALTHY;
    }
}