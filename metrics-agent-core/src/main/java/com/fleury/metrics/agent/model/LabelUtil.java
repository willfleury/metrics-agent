package com.fleury.metrics.agent.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Type;

/**
 *
 * @author Will Fleury
 */
public class LabelUtil {

    public static Map<String, String> splitLabelNameAndValue(List<String> labels) {
        Map<String, String> names = new LinkedHashMap<String, String>();

        if (labels == null) {
            return names;
        }

        for (String label : labels) {
            String[] tokens = label.split(":");
            names.put(tokens[0].trim(), tokens[1].trim());
        }
        return names;
    }
    
    public static int getLabelVarIndex(String value) {
        if (isLabelVarNested(value)) {
            return Integer.valueOf(value.substring(1, value.indexOf('.')));
        }

        return Integer.valueOf(value.substring(1, value.length()));
    }

    public static String getNestedLabelVar(String value) {
        return value.substring(value.indexOf('.') + 1, value.length());
    }

    public static boolean isLabelVarNested(String value) {
        return value.contains(".");
    }
    
    public static boolean isThis(String value) {
        return value.startsWith("$this");
    }
    
    public static boolean isTemplatedLabelValue(String value) {
        return value.startsWith("$");
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

    public static void validateLabelValues(String method, List<String> labels, Type[] argTypes) {
        List<String> values = getLabelValues(labels);

        for (String value : values) {
            new LabelValidator(method, argTypes).validate(value);
        }
    }
}
