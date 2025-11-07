package com.retry.budget.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyStats {
    
    private double p50;
    private double p95;
    private double p99;
    private double mean;
    private double standardDeviation;
    private long min;
    private long max;
    private int sampleSize;
}