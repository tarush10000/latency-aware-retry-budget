package com.retry.budget.model;

import com.retry.budget.enums.ServiceHealthStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryBudget {
    
    private String serviceName;
    private int allocatedBudget;
    private int usedBudget;
    private int remainingBudget;
    private ServiceHealthStatus healthStatus;
    private double latencyFactor;
    private double errorFactor;
    private LocalDateTime lastCalculated;
    private LocalDateTime expiresAt;
    
    public boolean canRetry() {
        return remainingBudget > 0 && 
               healthStatus != ServiceHealthStatus.CRITICAL &&
               LocalDateTime.now().isBefore(expiresAt);
    }
    
    public void consumeBudget() {
        if (remainingBudget > 0) {
            this.usedBudget++;
            this.remainingBudget--;
        }
    }
    
    public void resetBudget(int newBudget) {
        this.allocatedBudget = newBudget;
        this.usedBudget = 0;
        this.remainingBudget = newBudget;
        this.lastCalculated = LocalDateTime.now();
    }
}