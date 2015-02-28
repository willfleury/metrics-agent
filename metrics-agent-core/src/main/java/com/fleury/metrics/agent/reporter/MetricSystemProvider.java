package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public interface MetricSystemProvider {
	
	MetricSystem createMetricSystem();
}
