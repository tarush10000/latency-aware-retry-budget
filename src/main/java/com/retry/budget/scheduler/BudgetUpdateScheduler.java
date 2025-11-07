package com.retry.budget.scheduler;

import com.retry.budget.service.RetryBudgetControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BudgetUpdateScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(BudgetUpdateScheduler.class);
    
    private final RetryBudgetControllerService budgetControllerService;
    
    public BudgetUpdateScheduler(RetryBudgetControllerService budgetControllerService) {
        this.budgetControllerService = budgetControllerService;
    }
    
    @Scheduled(fixedRateString = "${retry.budget.update-interval-seconds}000")
    public void updateBudgets() {
        logger.info("Starting scheduled budget update");
        
        try {
            budgetControllerService.updateAllBudgets();
            logger.info("Budget update completed successfully");
        } catch (Exception e) {
            logger.error("Error during budget update: {}", e.getMessage(), e);
        }
    }
}