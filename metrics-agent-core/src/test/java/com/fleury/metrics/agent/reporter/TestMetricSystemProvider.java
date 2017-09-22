package com.fleury.metrics.agent.reporter;

import java.util.Map;

/**
 *
 * @author Will Fleury
 */
public class TestMetricSystemProvider implements MetricSystemProvider {

    @Override
    public MetricSystem createMetricSystem(Map<String, Object> configuration) {
        return new TestMetricSystem();
    }
}
