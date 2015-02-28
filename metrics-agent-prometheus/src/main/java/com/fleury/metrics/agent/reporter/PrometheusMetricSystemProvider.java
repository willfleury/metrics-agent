package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class PrometheusMetricSystemProvider implements MetricSystemProvider{

	@Override
	public MetricSystem createMetricSystem() {
		return new PrometheusMetricSystem();
	}

}
