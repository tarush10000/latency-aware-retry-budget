package com.retry.budget.controller;

import com.retry.budget.enums.RetryDecision;
import com.retry.budget.model.RetryBudget;
import com.retry.budget.service.BudgetEnforcementAgent;
import com.retry.budget.service.RetryBudgetControllerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
public class RetryBudgetController {
    
    private final RetryBudgetControllerService budgetControllerService;
    private final BudgetEnforcementAgent enforcementAgent;
    
    public RetryBudgetController(RetryBudgetControllerService budgetControllerService,
                                 BudgetEnforcementAgent enforcementAgent) {
        this.budgetControllerService = budgetControllerService;
        this.enforcementAgent = enforcementAgent;
    }
    
    @GetMapping("/{serviceName}")
    public ResponseEntity<RetryBudget> getBudget(@PathVariable String serviceName) {
        RetryBudget budget = budgetControllerService.getBudget(serviceName);
        return ResponseEntity.ok(budget);
    }
    
    @PostMapping("/calculate/{serviceName}")
    public ResponseEntity<RetryBudget> calculateBudget(@PathVariable String serviceName) {
        RetryBudget budget = budgetControllerService.calculateBudget(serviceName);
        return ResponseEntity.ok(budget);
    }
    
    @GetMapping("/check/{serviceName}")
    public ResponseEntity<Map<String, Object>> checkRetryAllowed(@PathVariable String serviceName) {
        RetryDecision decision = enforcementAgent.checkRetryAllowed(serviceName);
        RetryBudget budget = budgetControllerService.getBudget(serviceName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", serviceName);
        response.put("decision", decision);
        response.put("allowed", decision == RetryDecision.ALLOW);
        response.put("remainingBudget", budget.getRemainingBudget());
        response.put("healthStatus", budget.getHealthStatus());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/consume/{serviceName}")
    public ResponseEntity<Map<String, String>> consumeBudget(@PathVariable String serviceName) {
        enforcementAgent.consumeBudget(serviceName);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Budget consumed for service: " + serviceName);
        response.put("serviceName", serviceName);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/update-all")
    public ResponseEntity<Map<String, String>> updateAllBudgets() {
        budgetControllerService.updateAllBudgets();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All budgets updated successfully");
        
        return ResponseEntity.ok(response);
    }
}