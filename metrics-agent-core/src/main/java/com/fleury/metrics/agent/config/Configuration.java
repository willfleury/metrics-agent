package com.fleury.metrics.agent.config;

import static java.util.logging.Level.FINE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.objectweb.asm.Type;

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
            configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
            setVisibilityChecker(getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
            registerModule(new ConfigSimpleModule());
        }
    };

    public static Configuration createConfig(String filename) {
        if (filename == null) {
            return emptyConfiguration();
        }

        LOGGER.log(FINE, "Found config file: {0}", filename);

        try {
            return createConfig(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration createConfig(InputStream is) {
        try {
            return MAPPER.readValue(is, Configuration.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {}
        }
    }

    public static Configuration emptyConfiguration() {
        return new Configuration();
    }

    private final Set<String> imports;
    private final Map<Key, List<Metric>> metrics;
    private final Map<String, Object> system;
    private final List<String> whiteList;
    private final List<String> blackList;

    private Configuration() {
        this(new HashMap<Key, List<Metric>>(),
                Collections.<String>emptySet(),
                Collections.<String, Object>emptyMap(),
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
    }

    @JsonCreator
    public Configuration(
            @JsonProperty("metrics") Map<Key, List<Metric>> metrics,
            @JsonProperty("imports") Set<String> imports,
            @JsonProperty("system") Map<String, Object> system,
            @JsonProperty("whiteList") List<String> whiteList,
            @JsonProperty("blackList") List<String> blackList) {

        this.imports = imports == null ? Collections.<String>emptySet() : imports;

        this.metrics = metrics == null ?
                new HashMap<Key, List<Metric>>() :
                processClassImports(metrics, this.imports); //ensure fqn expanded from imports

        this.system = system == null ? Collections.<String, Object>emptyMap() : system;
        this.whiteList = whiteList == null ? Collections.<String>emptyList() : whiteList;
        this.blackList = blackList == null ? Collections.<String>emptyList() : blackList;
    }

    private static Map<Key, List<Metric>> processClassImports(Map<Key, List<Metric>> metrics, Set<String> imports) {
        Map<String, String> expandedKeys = fqnToMap(imports);

        Map<Key, List<Metric>> processed = new HashMap<Key, List<Metric>>();
        for (Map.Entry<Key, List<Metric>> entry : metrics.entrySet()) {
            Key key = entry.getKey();

            String fqn = expandedKeys.get(key.getClassName());
            if (fqn == null) {
                fqn = key.getClassName();
            }

            String descriptor = key.descriptor;

            Map<String, String> fqnMap = getMethodDescriptorFQNMap(descriptor);
            for (String className : fqnMap.keySet()) {
                if (expandedKeys.containsKey(className)) {
                    descriptor = descriptor.replaceAll(className, expandedKeys.get(className));
                }
            }

            key = new Key(fqn, key.getMethod(), descriptor);

            processed.put(key, entry.getValue());
        }

        return processed;
    }


    public boolean isMetric(String className) {
        for (Key key : metrics.keySet()) {
            if (key.className.equals(className)) {
                return true;
            }
        }

        return false;
    }

    public List<Metric> findMetrics(String className) {
        if (metrics.isEmpty()) return Collections.emptyList();

        List<Metric> found = new ArrayList<Metric>();
        for (Key key : metrics.keySet()) {
            if (key.className.equals(className)) {
                found.addAll(metrics.get(key));
            }
        }

        return found;
    }

    public List<Metric> findMetrics(String className, String method, String descriptor) {
        Key key = new Key(className, method, descriptor);
        return metrics.containsKey(key) ? metrics.get(key) : Collections.<Metric>emptyList();
    }

    public void addMetric(Key key, Metric metric) {
        List<Metric> keyMetrics = metrics.get(key);

        if (keyMetrics == null) {
            keyMetrics = new ArrayList<Metric>();
            metrics.put(key, keyMetrics);
        }

        keyMetrics.add(metric);
    }

    public Map<String, Object> getSystem() {
        return system;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public List<String> getBlackList() {
        return blackList;
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
                ", system=" + system +
                ", whiteList=" + whiteList +
                ", blackList=" + blackList +
                '}';
    }

    public static Map<String, String> fqnToMap(Collection<String> classNames) {
        Map<String, String> expandedKeys = new HashMap<String, String>();
        for (String fqn : classNames) {
            String className = fqn.substring(fqn.lastIndexOf("/") + 1, fqn.length());
            expandedKeys.put(className, fqn);
        }

        return expandedKeys;
    }

    public static Map<String, String> getMethodDescriptorFQNMap(String descriptor) {
        Type type = Type.getMethodType(descriptor);

        Set<String> classes = new HashSet<String>();
        classes.add(type.getReturnType().getClassName());

        Type[] arguments = type.getArgumentTypes();
        if (arguments != null) {
            for (Type arg : arguments) {
                classes.add(arg.getClassName());
            }
        }

        return fqnToMap(classes);
    }

    public static class Key {

        private final String className;
        private final String method;
        private final String descriptor;

        public Key(String className, String method, String descriptor) {
            this.className = className;
            this.method = method;
            this.descriptor = descriptor;
        }

        public String getClassName() {
            return className;
        }

        public String getMethod() {
            return method;
        }

        public String getDescriptor() {
            return descriptor;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.className != null ? this.className.hashCode() : 0);
            hash = 29 * hash + (this.method != null ? this.method.hashCode() : 0);
            hash = 29 * hash + (this.descriptor != null ? this.descriptor.hashCode() : 0);
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
            if ((this.descriptor == null) ? (other.descriptor != null) : !this.descriptor.equals(other.descriptor)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "className='" + className + '\'' +
                    ", method='" + method + '\'' +
                    ", desc='" + descriptor + '\'' +
                    '}';
        }
    }

    static class MetricKey extends KeyDeserializer {

        @Override
        public Object deserializeKey(final String key, final DeserializationContext ctxt) throws IOException {
            String className = dotToSlash(key.substring(0, key.lastIndexOf(".")));
            String methodName = key.substring(key.lastIndexOf(".") + 1, key.indexOf("("));

            String desc = key.substring(key.indexOf("("), key.length());

            return new Key(className, methodName, desc);
        }
    }

    static class ConfigSimpleModule extends SimpleModule {

        public ConfigSimpleModule() {
            addKeyDeserializer(Key.class, new MetricKey());
        }

    }

}
