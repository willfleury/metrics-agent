package com.fleury.metrics.agent.reporter;

import com.fleury.metrics.agent.annotation.Counted;
import org.junit.Test;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class PrometheusMetricSystemTest {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(PrometheusMetricSystemTest.class);
	
	protected MetricSystem metrics;

	@Before
	public void setup() {
		metrics = Reporter.METRIC_SYSTEM;
	}
	
	@Test
	public void testPrometheusCounter() {
		
	}
	
	public static class CountedConstructorClass {
		
		@Counted(name = "constructor", labels = {"count"})
		public CountedConstructorClass() {
			
		}
	}
}
