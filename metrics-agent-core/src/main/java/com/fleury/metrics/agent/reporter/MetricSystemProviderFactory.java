package com.fleury.metrics.agent.reporter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 *
 * @author Will Fleury
 */
public class MetricSystemProviderFactory {
	
	public static final MetricSystemProviderFactory INSTANCE = new MetricSystemProviderFactory();
	
	private final MetricSystemProvider system;

	private MetricSystemProviderFactory() {
		this.system = initialiseMetricSystem();
	}
	
	public MetricSystemProvider getProvider() {
		return system;
	}
	
	private MetricSystemProvider initialiseMetricSystem() {
		final ServiceLoader<MetricSystemProvider> loader = ServiceLoader.load(MetricSystemProvider.class);
		List<MetricSystemProvider> integrations = new ArrayList<MetricSystemProvider>();
		
		Iterator<MetricSystemProvider> iterator = loader.iterator();
		while (iterator.hasNext()) {
			integrations.add(iterator.next());
		}
		
		if (integrations.isEmpty()) {
			throw new IllegalStateException("You must attach at least one reporting system to classpath");
		}
		
		if (integrations.size() > 1) {
			throw new IllegalStateException("More than one reporting system found on classpath. There can only be one.");
		} 
		
		MetricSystemProvider first = integrations.get(0);
		
		return first;
	}
}
