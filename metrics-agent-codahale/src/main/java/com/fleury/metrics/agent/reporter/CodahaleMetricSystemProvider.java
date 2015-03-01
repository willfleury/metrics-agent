package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury
 */
public class CodahaleMetricSystemProvider implements MetricSystemProvider {

	@Override
	public MetricSystem createMetricSystem() {
		return new CodahaleMetricSystem();
	}

}
