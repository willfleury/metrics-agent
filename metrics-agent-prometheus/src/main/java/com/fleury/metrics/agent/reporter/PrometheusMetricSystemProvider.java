package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury
 */
public class PrometheusMetricSystemProvider implements MetricSystemProvider {

    @Override
    public MetricSystem createMetricSystem() {
        return new PrometheusMetricSystem();
    }

}
