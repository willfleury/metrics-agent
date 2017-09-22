package com.fleury.metrics.agent.transformer.asm.injector;

import static org.junit.Assert.assertEquals;

import com.fleury.metrics.agent.annotation.Counted;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class LabelsTest extends BaseMetricTest {

    @Test
    public void shouldCountConstructorInvocationWithLabels() throws Exception {
        testInvocation(CountedConstructorWithLabelsClass.class, new String[]{"value1", "value2"});
    }

    @Test
    public void shouldCountConstructorInvocationWithoutLabels() throws Exception {
        testInvocation(CountedConstructorWithoutLabelsClass.class, null);
    }

    @Test
    public void shouldCountConstructorInvocationWithEmptyLabels() throws Exception {
        testInvocation(CountedConstructorWithEmptyLabelsClass.class, null);
    }

    @Test
    public void shouldCountConstructorInvocationWithDynamicStringLabelValue() throws Exception {
        testInvocationWithArgs(CountedConstructorWithDynamicStringLabelValueClass.class,
                new Object[]{"hello"}, new String[]{"hello"});
    }
    
    @Test
    public void shouldCountMethodInvocationWithDynamicValueThis() throws Exception {
        testMethodInvocation(CountedMethodWithDynamicLabelValueThisClass.class, new String[]{"hello"});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithInvalueDynamicLabelValueThisInConstructor() throws Exception {
        testInvocation(CountedConstructorWithDynamicLabelValueThisClass.class, new String[]{"hello"});
    }

    @Test
    public void shouldCountConstructorInvocationWithDynamicLongValue() throws Exception {
        testInvocationWithArgs(CountedConstructorWithDynamicLongLabelValueClass.class,
                new Object[]{0, 5}, new String[]{"5"});
    }

    @Test
    public void shouldCountConstructorInvocationWithDynamicNestedValue() throws Exception {
        testInvocationWithArgs(CountedConstructorWithDynamicNestedLabelValueClass.class,
                new Object[]{new CountedConstructorWithDynamicNestedLabelValueClass.Nester()}, new String[]{"hello"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenInvalidParamIndexLabelValue() throws Exception {
        testInvocationWithArgs(CountedConstructorWithInvalidParamIndexLabelValueClass.class,
                new Object[]{5}, new String[]{"5"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenInvalidDynamicLabelValue() throws Exception {
        testInvocationWithArgs(CountedConstructorWithInvalidDynamicLabelValueClass.class,
                new Object[]{5}, new String[]{"5"});
    }

    private void testInvocation(Class instrumentClazz, String[] labelValues) throws Exception {
        Class<?> clazz = execute(instrumentClazz);

        clazz.newInstance();

        assertEquals(1, metrics.getCount("constructor", labelValues));
    }

    private void testInvocationWithArgs(Class instrumentClazz, Object[] args, String[] labelValues) throws Exception {
        Class<?> clazz = execute(instrumentClazz);

        clazz.getConstructors()[0].newInstance(args);

        assertEquals(1, metrics.getCount("constructor", labelValues));
    }
    
    private void testMethodInvocation(Class instrumentClazz, String[] labelValues) throws Exception {
        Class<?> clazz = execute(instrumentClazz);
        Object obj = clazz.newInstance();

        obj.getClass().getMethod("method").invoke(obj);
        
        assertEquals(1, metrics.getCount("method", labelValues));
    }

    public static class CountedConstructorWithLabelsClass {

        @Counted(name = "constructor", labels = {"name1:value1", "name2:value2"})
        public CountedConstructorWithLabelsClass() {
            BaseMetricTest.performBasicTask();
        }
    }

    public static class CountedConstructorWithoutLabelsClass {

        @Counted(name = "constructor")
        public CountedConstructorWithoutLabelsClass() {
            BaseMetricTest.performBasicTask();
        }
    }

    public static class CountedConstructorWithEmptyLabelsClass {

        @Counted(name = "constructor", labels = {})
        public CountedConstructorWithEmptyLabelsClass() {
            BaseMetricTest.performBasicTask();
        }
    }
    
    public static class CountedConstructorWithDynamicLabelValueThisClass {
        @Counted(name = "constructor", labels = {"name1:$this"})
        public CountedConstructorWithDynamicLabelValueThisClass() {
            BaseMetricTest.performBasicTask();
        }
    }
    
     public static class CountedMethodWithDynamicLabelValueThisClass {
        
        public CountedMethodWithDynamicLabelValueThisClass() {
        }
        
         @Counted(name = "method", labels = {"name1:$this"})
        public void method() {
            BaseMetricTest.performBasicTask();
        }
        
        @Override
        public String toString() {
            return "hello";
        }
    }

    public static class CountedConstructorWithDynamicStringLabelValueClass {

        @Counted(name = "constructor", labels = {"name1:$0"})
        public CountedConstructorWithDynamicStringLabelValueClass(String value) {
            BaseMetricTest.performBasicTask();
        }
    }

    public static class CountedConstructorWithDynamicLongLabelValueClass {

        @Counted(name = "constructor", labels = {"name1:$1"})
        public CountedConstructorWithDynamicLongLabelValueClass(long rand, long value) {
            BaseMetricTest.performBasicTask();
        }
    }

    public static class CountedConstructorWithDynamicNestedLabelValueClass {

        public static class Nester {
            public String getHello() {
                return "hello";
            }
        }

        @Counted(name = "constructor", labels = {"name1:$0.hello"})
        public CountedConstructorWithDynamicNestedLabelValueClass(Nester nester) {
            BaseMetricTest.performBasicTask();
        }
    }

    public static class CountedConstructorWithInvalidParamIndexLabelValueClass {

        @Counted(name = "constructor", labels = {"name1:$5"})
        public CountedConstructorWithInvalidParamIndexLabelValueClass(long value) {
            BaseMetricTest.performBasicTask();
        }
    }

    public static class CountedConstructorWithInvalidDynamicLabelValueClass {

        @Counted(name = "constructor", labels = {"name1:$badlabel"})
        public CountedConstructorWithInvalidDynamicLabelValueClass(long value) {
            BaseMetricTest.performBasicTask();
        }
    }
}
