package com.retry.budget.util;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PercentileCalculator {
    
    public double calculatePercentile(List<Long> values, double percentile) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Long value : values) {
            stats.addValue(value);
        }
        
        return stats.getPercentile(percentile);
    }
    
    public double calculateMean(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Long value : values) {
            stats.addValue(value);
        }
        
        return stats.getMean();
    }
    
    public double calculateStandardDeviation(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Long value : values) {
            stats.addValue(value);
        }
        
        return stats.getStandardDeviation();
    }
}