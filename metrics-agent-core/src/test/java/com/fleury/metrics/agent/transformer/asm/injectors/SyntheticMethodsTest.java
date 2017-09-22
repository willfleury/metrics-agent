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
     * The following generics results in two methods with the same name in the CountedMethodClass bytecode.. This
     * confused the annotation scanning unless you also check the method access codes
     *
     * public counted(Lcom/fleury/metrics/agent/transformer/asm/injectors/OverrideMethodAnnotationTest$B;)V
     *
     * public synthetic bridge counted(Lcom/fleury/metrics/agent/transformer/asm/injectors/OverrideMethodAnnotationTest$A;)V
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
