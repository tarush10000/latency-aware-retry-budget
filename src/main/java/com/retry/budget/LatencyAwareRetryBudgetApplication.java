package com.retry.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync
public class LatencyAwareRetryBudgetApplication {

    public static void main(String[] args) {
        SpringApplication.run(LatencyAwareRetryBudgetApplication.class, args);
        System.out.println("Latency-Aware Retry Budget Allocator Started Successfully");
    }
}