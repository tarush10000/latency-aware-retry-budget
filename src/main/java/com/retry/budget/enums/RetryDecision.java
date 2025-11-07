package com.retry.budget.enums;

public enum RetryDecision {
    ALLOW("Retry is allowed"),
    DENY("Retry is denied - budget exceeded"),
    DEFER("Retry is deferred - service degraded");
    
    private final String description;
    
    RetryDecision(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}