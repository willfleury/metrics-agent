package com.fleury.metrics.agent.transformer.asm.injector;

import com.fleury.metrics.agent.annotation.ExceptionCounted;
import com.fleury.metrics.agent.annotation.Timed;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class TimedExceptionCountedInjectorTest extends BaseMetricTest {

    @Test
    public void shouldRecordConstructorInvocationStatistics() throws Exception {
        Class<TimedExceptionCountedConstructorClass> clazz = execute(TimedExceptionCountedConstructorClass.class);

        Object obj = clazz.newInstance();

        long[] values = metrics.getTimes("constructor", new String[]{"timed"});
        assertEquals(1, values.length);
        assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));

        assertEquals(0, metrics.getCount("constructor", new String[]{"exception"}));
    }

    @Test
    public void shouldRecordMethodInvocationWhenExceptionThrownStatistics() throws Exception {
        Class<TimedExceptionCountedMethodClassWithException> clazz = execute(TimedExceptionCountedMethodClassWithException.class);

        Object obj = clazz.newInstance();

        boolean exceptionOccured = false;
        try {
            obj.getClass().getMethod("timed").invoke(obj);
        }
        catch (InvocationTargetException e) {
            exceptionOccured = true;
        }

        assertTrue(exceptionOccured);

        long[] values = metrics.getTimes("timed", new String[]{"timed"});
        assertEquals(1, values.length);
        assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));

        assertEquals(1, metrics.getCount("timed", new String[]{"exception"}));
    }

    public static class TimedExceptionCountedConstructorClass {

        @Timed(name = "constructor", labels = {"type:timed"})
        @ExceptionCounted(name = "constructor", labels = {"type:exception"})
        public TimedExceptionCountedConstructorClass() {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
            }
        }
    }

    public static class TimedExceptionCountedMethodClassWithException {

        @Timed(name = "timed", labels = {"type:timed"})
        @ExceptionCounted(name = "timed", labels = {"type:exception"})
        public void timed() {
            try {
                Thread.sleep(10L);
                callService();
            }
            catch (InterruptedException e) {
            } 
        }
        
        public final void callService() {
            BaseMetricTest.performBasicTask();
            throw new RuntimeException();
        }
    }
}
