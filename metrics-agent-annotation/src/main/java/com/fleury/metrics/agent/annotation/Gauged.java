package com.fleury.metrics.agent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Will Fleury
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Gauged {

    enum mode {
        in_flight
    }

    String name();

    mode mode() default mode.in_flight;

    String[] labels() default {};

    String doc() default "";
}
