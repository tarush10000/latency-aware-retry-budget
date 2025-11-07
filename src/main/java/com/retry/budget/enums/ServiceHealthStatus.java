package com.retry.budget.enums;

public enum ServiceHealthStatus {
    HEALTHY("Service is operating normally"),
    DEGRADED("Service is experiencing issues"),
    CRITICAL("Service is severely impaired");
    
    private final String description;
    
    ServiceHealthStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}