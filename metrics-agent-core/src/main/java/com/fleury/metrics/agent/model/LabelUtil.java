package com.fleury.metrics.agent.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class LabelUtil {
	
	public static Map<String, String> splitLabelNameAndValue(List<String> labels) {
		Map<String, String> names = new LinkedHashMap<String, String>();
		
		if (labels == null) { return names; }
		
		for (String label : labels) {
			String[] tokens = label.split(":");
			names.put(tokens[0].trim(), tokens[1].trim());
		}
		return names;
	}

	public static List<String> getLabelNames(List<String> labels) {
		return new ArrayList<String>(splitLabelNameAndValue(labels).keySet());
	}
	
	public static String[] getLabelNamesAsArray(List<String> labels) {
		return getLabelNames(labels).toArray(new String[0]);
	}
	
	public static List<String> getLabelValues(List<String> labels) {
		return new ArrayList<String>(splitLabelNameAndValue(labels).values());
	}
	
	public static int getLabelValueVarIndex(String labelValue) {
		if (!labelValue.matches("\\$[0-9]+")) {
			throw new IllegalArgumentException("Templated label variable must match pattern $[0-9]+");
		}
		
		return Integer.valueOf(labelValue.substring(1, labelValue.length()));
	}
}
