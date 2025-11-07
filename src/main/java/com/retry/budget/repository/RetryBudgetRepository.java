package com.retry.budget.repository;

import com.retry.budget.model.RetryBudget;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RetryBudgetRepository {
    
    private static final String BUDGET_KEY_PREFIX = "budget:";
    private static final long TTL_SECONDS = 300;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RetryBudgetRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void saveBudget(RetryBudget budget) {
        String key = BUDGET_KEY_PREFIX + budget.getServiceName();
        redisTemplate.opsForValue().set(key, budget, TTL_SECONDS, TimeUnit.SECONDS);
    }
    
    public RetryBudget getBudget(String serviceName) {
        String key = BUDGET_KEY_PREFIX + serviceName;
        return (RetryBudget) redisTemplate.opsForValue().get(key);
    }
    
    public void deleteBudget(String serviceName) {
        String key = BUDGET_KEY_PREFIX + serviceName;
        redisTemplate.delete(key);
    }
    
    public Set<String> getAllServiceNames() {
        Set<String> keys = redisTemplate.keys(BUDGET_KEY_PREFIX + "*");
        return keys.stream()
                .map(key -> key.replace(BUDGET_KEY_PREFIX, ""))
                .collect(Collectors.toSet());
    }
}