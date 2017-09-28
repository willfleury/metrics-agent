package com.fleury.metrics.agent.transformer.asm.util;

import static com.fleury.metrics.agent.model.MetricType.Counted;
import static com.fleury.metrics.agent.model.MetricType.ExceptionCounted;
import static com.fleury.metrics.agent.model.MetricType.Gauged;
import static com.fleury.metrics.agent.model.MetricType.Timed;

import com.fleury.metrics.agent.model.MetricType;

/**
 *
 * @author Will Fleury
 */
public class AnnotationUtil {

    public static MetricType checkSignature(String desc) {
        if (isAnnotationPresent(desc, Counted)) {
            return Counted;
        }

        if (isAnnotationPresent(desc, Gauged)) {
            return Gauged;
        }

        if (isAnnotationPresent(desc, Timed)) {
            return Timed;
        }

        if (isAnnotationPresent(desc, ExceptionCounted)) {
            return ExceptionCounted;
        }

        return null;
    }

    public static boolean isAnnotationPresent(String desc, MetricType annotation) {
        return annotation.getDesc().equals(desc);
    }
}

