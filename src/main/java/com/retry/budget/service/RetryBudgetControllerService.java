package com.retry.budget.service;

import com.retry.budget.model.RetryBudget;

public interface RetryBudgetControllerService {
    
    RetryBudget calculateBudget(String serviceName);
    
    RetryBudget getBudget(String serviceName);
    
    void updateAllBudgets();
}