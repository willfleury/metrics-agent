package com.fleury.metrics.agent.model;

import com.fleury.metrics.agent.annotation.Counted;
import com.fleury.metrics.agent.annotation.ExceptionCounted;
import com.fleury.metrics.agent.annotation.Gauged;
import com.fleury.metrics.agent.annotation.Timed;
import org.objectweb.asm.Type;

/**
 *
 * @author Will Fleury
 */
public enum MetricType {
	Counted(Counted.class),
	Gauged(Gauged.class),
	Timed(Timed.class),
	ExceptionCounted(ExceptionCounted.class);
	
	private final Class annotation;
	private final String desc;

	MetricType(Class annotation) {
		this.annotation = annotation;
		this.desc = Type.getDescriptor(annotation);
	}

	public Class getAnnotation() {
		return annotation;
	}
	
	public String getDesc() {
		return desc;
	}
}
