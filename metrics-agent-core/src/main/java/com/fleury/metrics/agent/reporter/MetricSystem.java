package com.fleury.metrics.agent.reporter;

/**
 *
 * @author Will Fleury
 */
public interface MetricSystem {

    void registerGauge(String name, String[] labelNames, String doc);

    void registerCounter(String name, String[] labelNames, String doc);

    void registerTimer(String name, String[] labelNames, String doc);

    void recordCount(String name, String[] labelValues);

    void recordCount(String name, String[] labelValues, long n);

    void recordGaugeInc(String name, String[] labelValues);

    void recordGaugeDec(String name, String[] labelValues);

    void recordTime(String name, String[] labelValues, long duration);

    void startDefaultEndpoint();

}
