package com.fleury.metrics.agent.reporter;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Will Fleury
 */
public class CodahaleMetricSystem implements MetricSystem {

    private static final Logger LOGGER = Logger.getLogger(CodahaleMetricSystem.class.getName());

    private final MetricRegistry registry = new MetricRegistry();
    private final Map<String, String> configuration;

    public CodahaleMetricSystem(Map<String, String> configuration) {
        this.configuration = configuration;
        startJmxReporter();
    }

    @Override
    public void registerCounter(String name, String labelNames[], String doc) {
    }

    @Override
    public void registerGauge(String name, String[] labelNames, String doc) {
    }

    @Override
    public void registerTimer(String name, String labelNames[], String doc) {
    }

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
        LOGGER.fine("Starting JMX reporter");

        JmxReporter
                .forRegistry(registry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.MINUTES)
                .build()
                .start();
    }

}
