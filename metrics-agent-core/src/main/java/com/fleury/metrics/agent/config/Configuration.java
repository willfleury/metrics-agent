package com.fleury.metrics.agent.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
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

/**
 *
 * @author Will Fleury
 */
public class Configuration {
	
	private final static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()) {{
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		setVisibilityChecker(getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
		registerModule(new ConfigSimpleModule());
	}};
	
	private Map<Key, List<Metric>> metrics;

	public Configuration() {
		this (new HashMap<Key, List<Metric>>());
	}
	
	public Configuration(Map<Key, List<Metric>> metrics) {
		this.metrics = metrics;
	}
	
	
	public List<Metric> findMetrics(String className, String method) {
		Key key = new Key(className, method);
		return metrics.containsKey(key) ? metrics.get(key) : Collections.<Metric>emptyList();
	}
	
	public Map<Key, List<Metric>> getAllMetrics() {
		return metrics;
	}
	
	public static Configuration createConfig(String filename) {
		if (filename == null) {
			return new Configuration();
		}
		
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
		}
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
