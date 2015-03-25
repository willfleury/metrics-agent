package com.fleury.metrics.agent.transformer.asm.injector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fleury.metrics.agent.annotation.ExceptionCounted;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class ExceptionCounterInjectorTest extends BaseMetricTest {

    private final static String EXCEPTION_TEXT = "I've been thrown!";

    @Test
    public void shouldCountWhenExceptionThrownInConstructor() throws Exception {
        Class<ExceptionCountedConstructorClass> clazz = execute(ExceptionCountedConstructorClass.class);

        Exception thrown = null;
        try {
            Object obj = clazz.newInstance();
        }
        catch (Exception e) {
            thrown = e;
        }

        assertNotNull(thrown);
        assertTrue(thrown instanceof RuntimeException);
        assertEquals(EXCEPTION_TEXT, thrown.getMessage());

        assertEquals(1, metrics.getCount("constructor", new String[]{"type1"}));
    }

    @Test
    public void shouldCountAndRethrowWhenExceptionThrownInMethod() throws Exception {
        Class<ExceptionCountedMethodClass> clazz = execute(ExceptionCountedMethodClass.class);

        Object obj = clazz.newInstance();

        Exception thrown = null;
        try {
            obj.getClass().getMethod("exceptionCounted").invoke(obj);
        }
        catch (Exception e) {
            thrown = e;
        }

        assertNotNull(thrown);
        assertTrue(thrown instanceof InvocationTargetException);
        assertTrue(thrown.getCause() instanceof RuntimeException);
        assertEquals(EXCEPTION_TEXT, thrown.getCause().getMessage());

        assertEquals(1, metrics.getCount("exceptionCounted"));
    }

    @Test
    public void shouldNotCountWhenNoExceptionThrown() throws Exception {
        Class<ExceptionCountedMethodClassWithNoException> clazz = execute(ExceptionCountedMethodClassWithNoException.class);

        Object obj = clazz.newInstance();

        obj.getClass().getMethod("exceptionCounted").invoke(obj);

        assertEquals(0, metrics.getCount("exceptionCounted"));
    }

    public static class ExceptionCountedConstructorClass {

        @ExceptionCounted(name = "constructor", labels = "exception:type1")
        public ExceptionCountedConstructorClass() {
            callService();
        }
        
        public final void callService() {
            BaseMetricTest.performBasicTask();
            throw new RuntimeException(EXCEPTION_TEXT);
        }
    }

    public static class ExceptionCountedMethodClass {

        @ExceptionCounted(name = "exceptionCounted")
        public void exceptionCounted() {
            callService();
        }
        
        public final void callService() {
            BaseMetricTest.performBasicTask();
            throw new RuntimeException(EXCEPTION_TEXT);
        }
    }

    public static class ExceptionCountedMethodClassWithNoException {

        @ExceptionCounted(name = "exceptionCounted")
        public void exceptionCounted() {
            BaseMetricTest.performBasicTask();
        }
    }

}
