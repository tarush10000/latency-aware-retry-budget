package com.retry.budget.exception;

public class RetryBudgetExceededException extends RuntimeException {
    
    private final String serviceName;
    private final int remainingBudget;
    
    public RetryBudgetExceededException(String serviceName, int remainingBudget) {
        super(String.format("Retry budget exceeded for service '%s'. Remaining budget: %d", 
                serviceName, remainingBudget));
        this.serviceName = serviceName;
        this.remainingBudget = remainingBudget;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public int getRemainingBudget() {
        return remainingBudget;
    }
}