package com.retry.budget.repository;

import com.retry.budget.model.ServiceMetrics;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class MetricsRepository {
    
    private static final String METRICS_KEY_PREFIX = "metrics:";
    private static final long TTL_SECONDS = 300;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public MetricsRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void saveMetrics(ServiceMetrics metrics) {
        String key = METRICS_KEY_PREFIX + metrics.getServiceName();
        redisTemplate.opsForValue().set(key, metrics, TTL_SECONDS, TimeUnit.SECONDS);
    }
    
    public ServiceMetrics getMetrics(String serviceName) {
        String key = METRICS_KEY_PREFIX + serviceName;
        return (ServiceMetrics) redisTemplate.opsForValue().get(key);
    }
    
    public void deleteMetrics(String serviceName) {
        String key = METRICS_KEY_PREFIX + serviceName;
        redisTemplate.delete(key);
    }
}