package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class CodahaleMetricSystemProvider implements MetricSystemProvider {

	@Override
	public MetricSystem createMetricSystem() {
		return new CodahaleMetricSystem();
	}

}
