package com.fleury.metrics.agent.reporter;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.hotspot.StandardExports;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class PrometheusMetricSystem implements MetricSystem {
	
	private final static Map<String, Counter> COUNTERS = new ConcurrentHashMap<String, Counter>();
	private final static Map<String, Gauge> GAUGES = new ConcurrentHashMap<String, Gauge>();
	private final static Map<String, Summary> SUMMARIES = new ConcurrentHashMap<String, Summary>();
	
	protected PrometheusMetricSystem() {
		new StandardExports().register();
	}
	
	@Override
	public void registerCounter(String name, String[] labels, String doc) {
		Counter.Builder builder = (Counter.Builder)Counter.build().name(name).help(doc);
		if (labels != null) {
			builder.labelNames(labels);
		}
		
		COUNTERS.put(name, builder.register()); 
	}
	
	@Override
	public void registerGauge(String name, String[] labels, String doc) {
		Gauge.Builder builder = (Gauge.Builder)Gauge.build().name(name).help(doc);
		if (labels != null) {
			builder.labelNames(labels);
		}
		
		GAUGES.put(name, builder.register());
	}
	
	@Override
	public void registerTimer(String name, String[] labels, String doc) {
		Summary.Builder builder = (Summary.Builder)Summary.build().name(name).help(doc);
		if (labels != null) {
			builder.labelNames(labels);
		}
		
		SUMMARIES.put(name, builder.register()); 
	}

	@Override
	public void recordCount(String name, String[] labels) {
		Counter counter = COUNTERS.get(name);
		if (labels != null) {
			counter.labels(labels).inc(); 
		} 
		else {
			counter.inc();
		}
	}
	
	@Override
	public void recordGaugeInc(String name, String[] labelValues) {
		Gauge gauge = GAUGES.get(name);
		if (labelValues != null) {
			gauge.labels(labelValues).inc();
		}
		else {
			gauge.inc();
		}
	}
	
	@Override
	public void recordGaugeDec(String name, String[] labelValues) {
		Gauge gauge = GAUGES.get(name);
		if (labelValues != null) {
			gauge.labels(labelValues).dec();
		}
		else {
			gauge.dec();
		}
	}
	
	@Override
	public void recordCount(String name, String[] labels, long n) {
		Counter counter = COUNTERS.get(name);
		if (labels != null) {
			counter.labels(labels).inc(n); 
		} 
		else {
			counter.inc(n);
		}
	}

	@Override
	public void recordTime(String name, String[] labels, long duration) {
		Summary summary = SUMMARIES.get(name);
		if (labels != null) {
			summary.labels(labels).observe(duration);
		}
		else {
			summary.observe(duration);
		}
	}

}
