package com.fleury.metrics.agent.reporter;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class CodahaleMetricSystem implements MetricSystem {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CodahaleMetricSystem.class);
	
	private final MetricRegistry registry = new MetricRegistry();
	
	public CodahaleMetricSystem() {
		startJmxReporter();
	}
	
	@Override
	public void registerCounter(String name, String labelNames[], String doc) { }
	
	@Override
	public void registerGauge(String name, String[] labelNames, String doc) { }
	
	@Override
	public void registerTimer(String name, String labelNames[], String doc) {}

	@Override
	public void recordCount(String name, String[] labelValues) {
		registry.counter(getMetricName(name, createSingleLabelValue(labelValues))).inc();
	}

	@Override
	public void recordCount(String name, String[] labelValues, long n) {
		registry.counter(getMetricName(name, createSingleLabelValue(labelValues))).inc(n);
	}
	
	@Override
	public void recordGaugeInc(String name, String[] labelValues) {
		registry.counter(getMetricName(name, createSingleLabelValue(labelValues))).inc();
	}
	
	@Override
	public void recordGaugeDec(String name, String[] labelValues) {
		registry.counter(getMetricName(name, createSingleLabelValue(labelValues))).dec();
	}

	@Override
	public void recordTime(String name, String[] labelValues, long duration) {
		registry.timer(getMetricName(name, createSingleLabelValue(labelValues))).update(duration, TimeUnit.NANOSECONDS);
	}
	
	public static String getMetricName(String name, String label) {
		return label == null ? name : name + "." + label;
	}
	
	private String createSingleLabelValue(String[] labelValues) {
		if (labelValues == null || labelValues.length == 0) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < labelValues.length; i++) {
			builder.append(labelValues[i]);
			
			if (i < labelValues.length - 1) {
				builder.append(".");
			}
		}
		
		return builder.toString();
	}
	
	private void startJmxReporter() {
		LOGGER.debug("Starting JMX reporter");

		JmxReporter
				.forRegistry(registry)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.convertRatesTo(TimeUnit.MINUTES)
				.build()
				.start();
	}

}
