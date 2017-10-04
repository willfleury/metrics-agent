package com.fleury.metrics.agent.reporter;

import static java.util.logging.Level.WARNING;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 *
 * @author Will Fleury
 */
public class PrometheusMetricSystem implements MetricSystem {

    private static final Logger LOGGER = Logger.getLogger(PrometheusMetricSystem.class.getName());

    private static final Map<String, Counter> COUNTERS = new ConcurrentHashMap<String, Counter>();
    private static final Map<String, Gauge> GAUGES = new ConcurrentHashMap<String, Gauge>();
    private static final Map<String, Histogram> HISTOGRAMS = new ConcurrentHashMap<String, Histogram>();

    private static final int DEFAULT_HTTP_PORT = 9899;
    
    private final Map<String, Object> configuration;

    protected PrometheusMetricSystem(Map<String, Object> configuration) {
        this.configuration = configuration;

        new StandardExports().register();

        addJVMMetrics(configuration);

        startDefaultEndpoint();
    }

    @Override
    public void registerCounter(String name, String[] labels, String doc) {
        Counter.Builder builder = Counter.build().name(name).help(doc);
        if (labels != null) {
            builder.labelNames(labels);
        }

        COUNTERS.put(name, builder.register());
    }

    @Override
    public void registerGauge(String name, String[] labels, String doc) {
        Gauge.Builder builder = Gauge.build().name(name).help(doc);
        if (labels != null) {
            builder.labelNames(labels);
        }

        GAUGES.put(name, builder.register());
    }

    @Override
    public void registerTimer(String name, String[] labels, String doc) {
        Histogram.Builder builder = Histogram.build().name(name).help(doc);
        if (labels != null) {
            builder.labelNames(labels);
        }

        HISTOGRAMS.put(name, builder.register());
    }

    @Override
    public void recordCount(String name, String[] labels) {
        Counter counter = COUNTERS.get(name);
        if (labels != null) {
            counter.labels(labels).inc();
        } else {
            counter.inc();
        }
    }

    @Override
    public void recordGaugeInc(String name, String[] labelValues) {
        Gauge gauge = GAUGES.get(name);
        if (labelValues != null) {
            gauge.labels(labelValues).inc();
        } else {
            gauge.inc();
        }
    }

    @Override
    public void recordGaugeDec(String name, String[] labelValues) {
        Gauge gauge = GAUGES.get(name);
        if (labelValues != null) {
            gauge.labels(labelValues).dec();
        } else {
            gauge.dec();
        }
    }

    @Override
    public void recordCount(String name, String[] labels, long n) {
        Counter counter = COUNTERS.get(name);
        if (labels != null) {
            counter.labels(labels).inc(n);
        } else {
            counter.inc(n);
        }
    }

    @Override
    public void recordTime(String name, String[] labels, long duration) {
        Histogram summary = HISTOGRAMS.get(name);
        if (labels != null) {
            summary.labels(labels).observe(duration);
        } else {
            summary.observe(duration);
        }
    }

    @Override
    public void startDefaultEndpoint() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int port = DEFAULT_HTTP_PORT;

                if (configuration.containsKey("httpPort")) {
                    port = Integer.parseInt((String)configuration.get("httpPort"));
                }

                try {
                    LOGGER.fine("Starting Prometheus HttpServer on port " + port);

                    new HTTPServer(port);

                } catch (Exception e) { //widen scope in case of ClassNotFoundException on non oracle/sun JVM
                    LOGGER.log(WARNING, "Unable to register Prometheus HttpServer on port " + port, e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void addJVMMetrics(Map<String, Object> configuration) {
        if (!configuration.containsKey("jvm")) {
            return;
        }
        Set<String> jvmMetrics = new HashSet<String>((List<String>)configuration.get("jvm"));
        if (jvmMetrics.contains("gc")) {
            new GarbageCollectorExports().register();
        }

        if (jvmMetrics.contains("threads")) {
            new ThreadExports().register();
        }

        if (jvmMetrics.contains("memory")) {
            new MemoryPoolsExports().register();
        }

        if (jvmMetrics.contains("classloader")) {
            new ClassLoadingExports().register();
        }
    }

    void reset() {
        COUNTERS.clear();
        GAUGES.clear();
        HISTOGRAMS.clear();
        CollectorRegistry.defaultRegistry.clear();
    }
}
