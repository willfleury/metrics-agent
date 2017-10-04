package com.fleury.metrics.agent.reporter;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * @author Will Fleury
 */
public class DropwizardMetricSystem implements MetricSystem {

    private static final Logger LOGGER = Logger.getLogger(DropwizardMetricSystem.class.getName());

    private final MetricRegistry registry = SharedMetricRegistries.getOrCreate("agent-metrics");
    private final Map<String, Object> configuration;

    public DropwizardMetricSystem(Map<String, Object> configuration) {
        this.configuration = configuration;
        addJVMMetrics(configuration);

        startDefaultEndpoint();
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

    @Override
    public void startDefaultEndpoint() {
        String domain = (String)configuration.get("domain");
        if (domain == null) {
            domain = "metrics";
        }

        LOGGER.fine("Starting JMX reporter at domain: " + domain);

        JmxReporter
                .forRegistry(registry)
                .inDomain(domain)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.MINUTES)
                .build()
                .start();
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

    private void addJVMMetrics(Map<String, Object> configuration) {
        if (!configuration.containsKey("jvm")) {
            return;
        }
        Set<String> jvmMetrics = new HashSet<String>((List<String>)configuration.get("jvm"));
        if (jvmMetrics.contains("gc")) {
            registry.register("gc", new GarbageCollectorMetricSet());
        }

        if (jvmMetrics.contains("threads")) {
            registry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
        }

        if (jvmMetrics.contains("memory")) {
            registry.register("memory", new MemoryUsageGaugeSet());
        }

        if (jvmMetrics.contains("classloader")) {
            registry.register("memory", new ClassLoadingGaugeSet());
        }
    }
}
