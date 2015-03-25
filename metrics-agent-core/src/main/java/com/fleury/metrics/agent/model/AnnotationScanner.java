package com.fleury.metrics.agent.model;

import static com.fleury.metrics.agent.model.MetricType.Counted;
import static com.fleury.metrics.agent.model.MetricType.ExceptionCounted;
import static com.fleury.metrics.agent.model.MetricType.Gauged;
import static com.fleury.metrics.agent.model.MetricType.Timed;

import java.util.Map;

/**
 *
 * @author Will Fleury
 */
public class AnnotationScanner {

    private final Map<MetricType, Metric> registry;

    public AnnotationScanner(Map<MetricType, Metric> registry) {
        this.registry = registry;
    }

    public Metric checkSignature(String desc) {
        if (isAnnotationPresent(desc, Counted)) {
            Metric metadata = new Metric(Counted);
            registry.put(Counted, metadata);

            return metadata;
        }

        if (isAnnotationPresent(desc, Gauged)) {
            Metric metadata = new Metric(Gauged);
            registry.put(Gauged, metadata);

            return metadata;
        }

        if (isAnnotationPresent(desc, Timed)) {
            Metric metadata = new Metric(Timed);
            registry.put(Timed, metadata);

            return metadata;
        }

        if (isAnnotationPresent(desc, ExceptionCounted)) {
            Metric metadata = new Metric(ExceptionCounted);
            registry.put(ExceptionCounted, metadata);

            return metadata;
        }

        return null;
    }

    public boolean isAnnotationPresent(String desc, MetricType annotation) {
        return annotation.getDesc().equals(desc);
    }
}
