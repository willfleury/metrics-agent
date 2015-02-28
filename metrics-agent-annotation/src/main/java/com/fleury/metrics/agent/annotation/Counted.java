package com.fleury.metrics.agent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Counted {

	public String name();
	
	public String[] labels() default {};
	
	public String doc() default "";
}
