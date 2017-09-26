package com.fleury.metrics.agent.config;

import static java.util.logging.Level.FINE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fleury.metrics.agent.model.Metric;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Will Fleury
 */
public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    public static String dotToSlash(String name) {
        return name.replaceAll("\\.", "/");
    }

    private final static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()) {
        {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            setVisibilityChecker(getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            registerModule(new ConfigSimpleModule());
        }
    };

    public static Configuration createConfig(String filename) {
        if (filename == null) {
            return new Configuration();
        }

        try {
            LOGGER.log(FINE, "Found config file: {0}", filename);
            Configuration configuration = createConfig(new FileInputStream(filename));
            LOGGER.log(FINE, "Created config: {0}", configuration);
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

    @JsonProperty("metrics")
    private Map<Key, List<Metric>> metrics = Collections.emptyMap();

    @JsonProperty("system")
    private Map<String, Object> metricSystemConfiguration = Collections.emptyMap();

    @JsonProperty("whiteList")
    private List<String> whiteList = Collections.emptyList();

    @JsonProperty("blackList")
    private List<String> blackList = Collections.emptyList();

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
    
    public Map<String, Object> getMetricSystemConfiguration() {
        return metricSystemConfiguration;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }

    public boolean isWhiteListed(String className) {
        if (whiteList.isEmpty()) return true;

        for (String white : whiteList) {
            if (className.startsWith(white)) {
                return true;
            }
        }

        return false;
    }

    public boolean isBlackListed(String className) {
        if (blackList.isEmpty()) return false;

        for (String black : blackList) {
            if (className.startsWith(black)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "metrics=" + metrics +
                ", metricSystemConfiguration=" + metricSystemConfiguration +
                ", whiteList=" + whiteList +
                ", blackList=" + blackList +
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
        public Object deserializeKey(final String key, final DeserializationContext ctxt) throws IOException {
            String className = dotToSlash(key.substring(0, key.lastIndexOf(".")));
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
