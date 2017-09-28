package com.fleury.metrics.agent.transformer.asm.injectors;

import static org.junit.Assert.assertEquals;

import com.fleury.metrics.agent.annotation.Counted;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class SyntheticMethodsTest extends BaseMetricTest {

    /**
     *
     * The following generics results in two methods with the same annotation in the CountedMethodClass bytecode.. This
     * confused the annotation scanning as annotations are placed on both the real and synthetic method.. Therefore need
     * to check the method access code to ensure its not synthetic.
     *
     * public counted(Lcom/fleury/metrics/agent/transformer/asm/injectors/OverrideMethodAnnotationTest$B;)V
     * @Lcom/fleury/metrics/agent/annotation/Counted;(name="counted")
     *   ...
     *
     *
     * public synthetic bridge counted(Lcom/fleury/metrics/agent/transformer/asm/injectors/OverrideMethodAnnotationTest$A;)V
     * @Lcom/fleury/metrics/agent/annotation/Counted;(name="counted")
     *   ...
     *
     *  See https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html
     */


    @Test
    public void shouldCountMethodInvocation() throws Exception {
        Class<CountedMethodClass> clazz = execute(CountedMethodClass.class);

        Object obj = clazz.newInstance();

        obj.getClass().getMethod("counted", B.class).invoke(obj, new Object[] {new B()});

        assertEquals(1, metrics.getCount("counted"));

        //if bridge methods being instrumented also we would have two invocations here.. validate only happens once.
        assertEquals(1, metrics.getCounterRegistrations());
    }

    public static class A { }

    public static class B extends  A { }

    public static class BaseClass<T extends A> {
        public void counted(T value) { }
    }

    public static class CountedMethodClass extends BaseClass<B> {

        @Override
        @Counted(name = "counted")
        public void counted(B value) {
            BaseMetricTest.performBasicTask();
        }
    }

}
