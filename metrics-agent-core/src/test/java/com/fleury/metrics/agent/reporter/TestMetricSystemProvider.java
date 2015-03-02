package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury
 */
public class TestMetricSystemProvider implements MetricSystemProvider {

    @Override
    public MetricSystem createMetricSystem() {
        return new TestMetricSystem();
    }
}
