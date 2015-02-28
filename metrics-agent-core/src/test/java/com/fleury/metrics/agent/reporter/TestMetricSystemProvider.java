package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class TestMetricSystemProvider implements MetricSystemProvider {

	@Override
	public MetricSystem createMetricSystem() {
		return new TestMetricSystem();
	}
}
