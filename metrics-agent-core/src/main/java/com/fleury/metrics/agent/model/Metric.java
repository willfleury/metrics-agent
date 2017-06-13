package com.fleury.metrics.agent.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Will Fleury
 */
public class Metric {
   
    private MetricType type;
    private String name;
    private String doc;
    private List<String> labels;
    private Map<String, String> ext = new HashMap<String, String>();

    public Metric() {
    }

    public Metric(MetricType type) {
        this.type = type;
    }

    public MetricType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    @JsonAnyGetter
    public Map<String, String> getExt() {
        return ext;
    }

    @JsonAnySetter
    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", doc='" + doc + '\'' +
                ", labels=" + labels +
                ", ext=" + ext +
                '}';
    }
}
