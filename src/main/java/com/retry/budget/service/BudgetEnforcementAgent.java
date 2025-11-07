package com.retry.budget.service;

import com.retry.budget.enums.RetryDecision;

public interface BudgetEnforcementAgent {
    
    RetryDecision checkRetryAllowed(String serviceName);
    
    void consumeBudget(String serviceName);
    
    boolean isServiceHealthy(String serviceName);
}