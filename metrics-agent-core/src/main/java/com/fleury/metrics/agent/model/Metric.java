package com.fleury.metrics.agent.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Will Fleury
 */
public class Metric {

    public static Map<MetricType, Metric> mapByType(List<Metric> configMetrics) {
        Map<MetricType, Metric> metrics = new HashMap<MetricType, Metric>();

        for (Metric metric : configMetrics) {
            metrics.put(metric.getType(), metric);
        }

        return metrics;
    }
   
    private final MetricType type;
    private final String name;
    private final String doc;
    private final List<String> labels;
    private final String mode;


    @JsonCreator
    Metric(@JsonProperty("type") MetricType type,
                  @JsonProperty("name") String name,
                  @JsonProperty("doc") String doc,
                  @JsonProperty("labels") List<String> labels,
                  @JsonProperty("mode") String mode) {
        this.type = type;
        this.name = name;
        this.doc = doc;
        this.labels = labels;
        this.mode = mode;
    }

    public MetricType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getDoc() {
        return doc;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", doc='" + doc + '\'' +
                ", labels=" + labels +
                ", mode=" + mode +
                '}';
    }

    public static MetricBuilder builder() {
        return new MetricBuilder();
    }

    public static class MetricBuilder {
        private MetricType type;
        private String name;
        private String doc;
        private List<String> labels;
        private String mode;

        public MetricBuilder withType(MetricType type) {
            this.type = type;
            return this;
        }

        public MetricBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MetricBuilder withDoc(String doc) {
            this.doc = doc;
            return this;
        }

        public MetricBuilder withLabels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        public MetricBuilder withMode(String mode) {
            this.mode = mode;
            return this;
        }

        public Metric createMetric() {
            return new Metric(type, name, doc, labels, mode);
        }
    }

}
