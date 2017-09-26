package com.fleury.metrics.agent.reporter;

import static java.util.logging.Level.FINER;

import com.fleury.metrics.agent.model.LabelUtil;
import com.fleury.metrics.agent.model.Metric;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Will Fleury
 */
public class Reporter {

    private static final Logger LOGGER = Logger.getLogger(Reporter.class.getName());

    public static final MetricSystem METRIC_SYSTEM = MetricSystemProviderFactory.INSTANCE.createMetricSystem();

    public static void startDefaultMetricEndpoint() {
        METRIC_SYSTEM.startDefaultEndpoint();
    }

    public static void registerMetrics(Collection<Metric> metrics) {
        for (Metric metric : metrics) {
            switch (metric.getType()) {
                case Counted:
                case ExceptionCounted:
                    registerCounter(metric.getName(), getLabelNames(metric.getLabels()), metric.getDoc());
                    break;

                case Gauged:
                    registerGauge(metric.getName(), getLabelNames(metric.getLabels()), metric.getDoc());
                    break;

                case Timed:
                    registerTimer(metric.getName(), getLabelNames(metric.getLabels()), metric.getDoc());
                    break;

                default:
                    throw new RuntimeException("Unhandled metric registration for type: " + metric.getType());
            }
        }
    }

    public static void registerCounter(String name, String[] labelNames, String doc) {
        LOGGER.log(FINER, "registering metric name: {0} doc: {1}", new Object[] {name, doc});
        METRIC_SYSTEM.registerCounter(name, labelNames, doc);
    }

    public static void registerGauge(String name, String[] labelNames, String doc) {
        LOGGER.log(FINER, "registering metric name: {0} doc: {1}", new Object[] {name, doc});
        METRIC_SYSTEM.registerGauge(name, labelNames, doc);
    }

    public static void registerTimer(String name, String[] labelNames, String doc) {
        LOGGER.log(FINER, "registering metric name: {0} doc: {1}", new Object[] {name, doc});
        METRIC_SYSTEM.registerTimer(name, labelNames, doc);
    }

    public static void recordCount(String name, String[] labelValues) {
        METRIC_SYSTEM.recordCount(name, labelValues);
    }

    public static void recordCount(String name, String[] labelValues, long n) {
        METRIC_SYSTEM.recordCount(name, labelValues, n);
    }

    public static void recordGaugeInc(String name, String[] labelValues) {
        METRIC_SYSTEM.recordGaugeInc(name, labelValues);
    }

    public static void recordGaugeDec(String name, String[] labelValues) {
        METRIC_SYSTEM.recordGaugeDec(name, labelValues);
    }

    public static void recordTime(String name, String[] labelValues, long duration) {
        METRIC_SYSTEM.recordTime(name, labelValues, duration);
    }

    private static String[] getLabelNames(List<String> list) {
        return list == null ? null : LabelUtil.getLabelNamesAsArray(list);
    }
}
