package com.fleury.metrics.agent.reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Will Fleury
 */
public class TestMetricSystem implements MetricSystem {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<String, AtomicLong>();

    private final Map<String, List<Long>> timers = new ConcurrentHashMap<String, List<Long>>();

    private int counterRegistrations = 0;
    private int gaugeRegistrations = 0;
    private int timerRegistrations = 0;

    @Override
    public void registerCounter(String name, String[] labelNames, String doc) {
        counterRegistrations++;
    }

    @Override
    public void registerGauge(String name, String[] labelNames, String doc) {
        gaugeRegistrations++;
    }

    @Override
    public void registerTimer(String name, String[] labelNames, String doc) {
        timerRegistrations++;
    }

    @Override
    public void recordCount(String name, String[] labelValues) {
        getOrAddCounter(counters, getName(name, createSingleLabelValue(labelValues))).incrementAndGet();
    }

    @Override
    public void recordCount(String name, String[] labelValues, long n) {
        getOrAddCounter(counters, getName(name, createSingleLabelValue(labelValues))).addAndGet(n);
    }

    @Override
    public void recordGaugeInc(String name, String[] labelValues) {
        AtomicLong value = getOrAddCounter(counters, getName(name, createSingleLabelValue(labelValues)));
        value.incrementAndGet();
    }

    @Override
    public void recordGaugeDec(String name, String[] labelValues) {
        AtomicLong value = getOrAddCounter(counters, getName(name, createSingleLabelValue(labelValues)));
        value.decrementAndGet();
    }

    @Override
    public void recordTime(String name, String[] labelValues, long duration) {
        getOrAddTimer(timers, getName(name, createSingleLabelValue(labelValues))).add(duration);
    }

    public long getCount(String name) {
        return counters.containsKey(name) ? counters.get(name).get() : 0;
    }

    public long getCount(String name, String[] labelValues) {
        String key = getName(name, createSingleLabelValue(labelValues));
        return counters.containsKey(key) ? counters.get(key).get() : 0;
    }

    public long[] getTimes(String name) {
        return convertBoxedToPrimitive(timers.get(name));
    }

    public long[] getTimes(String name, String[] labelValues) {
        return convertBoxedToPrimitive(timers.get(getName(name, createSingleLabelValue(labelValues))));
    }

    public int getCounterRegistrations() {
        return counterRegistrations;
    }

    public int getGaugeRegistrations() {
        return gaugeRegistrations;
    }

    public int getTimerRegistrations() {
        return timerRegistrations;
    }

    private String getName(String name, String labelValue) {
        return labelValue == null ? name : name + "." + labelValue;
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

    public List<Long> getOrAddTimer(Map<String, List<Long>> registry, String name) {
        List<Long> value = registry.get(name);
        if (value == null) {
            value = new ArrayList<Long>();

            List<Long> existing = registry.get(name);
            if (existing == null) {
                existing = registry.put(name, value);
            }

            if (existing != null) {
                throw new IllegalArgumentException("A metric named " + name + " already exists");
            }
        }

        return value;
    }

    public AtomicLong getOrAddCounter(Map<String, AtomicLong> registry, String name) {
        AtomicLong value = registry.get(name);
        if (value == null) {
            value = new AtomicLong();

            AtomicLong existing = registry.get(name);
            if (existing == null) {
                existing = registry.put(name, value);
            }

            if (existing != null) {
                throw new IllegalArgumentException("A metric named " + name + " already exists");
            }
        }

        return value;
    }

    private long[] convertBoxedToPrimitive(List<Long> values) {
        long[] primitive = new long[values.size()];

        for (int i = 0; i < values.size(); i++) {
            primitive[i] = values.get(i);
        }

        return primitive;
    }

    public void reset() {
        counters.clear();
        timers.clear();
        counterRegistrations = 0;
        gaugeRegistrations = 0;
        timerRegistrations = 0;
    }

}
