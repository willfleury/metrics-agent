package com.fleury.metrics.agent.reporter;

import java.util.Map;

/**
 *
 * @author Will Fleury
 */
public interface MetricSystemProvider {

    MetricSystem createMetricSystem(Map<String, String> configuration);
}
