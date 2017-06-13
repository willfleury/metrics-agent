package com.fleury.metrics.agent.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fleury.metrics.agent.model.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Will Fleury
 */
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private final static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()) {
        {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            setVisibilityChecker(getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            registerModule(new ConfigSimpleModule());
        }
    };

    @JsonProperty("metrics")
    private Map<Key, List<Metric>> metrics;
    
    @JsonProperty("system")
    private Map<String, String> metricSystemConfiguration;

    private Set<String> whiteList;

    public Configuration() {
        this(new HashMap<Key, List<Metric>>());
    }

    public Configuration(Map<Key, List<Metric>> metrics) {
        this.metrics = metrics;
    }

    public List<Metric> findMetrics(String className, String method) {
        Key key = new Key(className, method);
        return metrics.containsKey(key) ? metrics.get(key) : Collections.<Metric>emptyList();
    }

    public Map<Key, List<Metric>> getMetrics() {
        return metrics;
    }
    
    public Map<String, String> getMetricSystemConfiguration() {
        return metricSystemConfiguration;
    }

    public static Configuration createConfig(String filename) {
        if (filename == null) {
            return new Configuration();
        }

        try {
            LOGGER.debug("Found config file: {}", filename);
            Configuration configuration = createConfig(new FileInputStream(filename));
            configuration.setWhiteList(configuration.createWhiteList());
            LOGGER.debug("Created config: {}", configuration);
            return configuration;
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration createConfig(InputStream is) {
        try {
            return MAPPER.readValue(is, Configuration.class);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> createWhiteList() {
        Set<String> result = new HashSet<String>();

        for (Key key : metrics.keySet()) {
            result.add(key.getClassName());
        }

        return result;
    }

    public boolean inWhiteList(String className) {
        return whiteList.contains(className);
    }

    public void setWhiteList(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "metrics=" + metrics +
                ", metricSystemConfiguration=" + metricSystemConfiguration +
                ", whiteList=" + whiteList +
                '}';
    }

    public static class Key {

        private final String className;
        private final String method;

        public Key(String className, String method) {
            this.className = className;
            this.method = method;
        }

        public String getClassName() {
            return className;
        }

        public String getMethod() {
            return method;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.className != null ? this.className.hashCode() : 0);
            hash = 29 * hash + (this.method != null ? this.method.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
                return false;
            }
            if ((this.method == null) ? (other.method != null) : !this.method.equals(other.method)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "className='" + className + '\'' +
                    ", method='" + method + '\'' +
                    '}';
        }
    }

    static class MetricKey extends KeyDeserializer {

        @Override
        public Object deserializeKey(final String key,
                final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String className = key.substring(0, key.lastIndexOf("."));
            String methodName = key.substring(key.lastIndexOf(".") + 1, key.length());

            return new Key(className, methodName);
        }
    }

    static class ConfigSimpleModule extends SimpleModule {

        public ConfigSimpleModule() {
            addKeyDeserializer(Key.class, new MetricKey());
        }

    }

}
