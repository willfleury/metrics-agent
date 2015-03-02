package com.fleury.metrics.agent.transformer.asm.injector;

import com.fleury.metrics.agent.annotation.Counted;
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
public class MixedInjectorTest extends BaseMetricTest {

    @Test
    public void shouldRecordConstructorInvocationStatistics() throws Exception {
        Class<MixedMetricConstructorClass> clazz = execute(MixedMetricConstructorClass.class);

        Object obj = clazz.newInstance();

        long[] values = metrics.getTimes("constructor", new String[]{"timed"});
        assertEquals(1, values.length);
        assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));

        assertEquals(1, metrics.getCount("constructor", new String[]{"counted"}));
        assertEquals(0, metrics.getCount("constructor", new String[]{"exception"}));
    }

    @Test
    public void shouldRecordMethodInvocationStatistics() throws Exception {
        Class<MixedMetricMethodClass> clazz = execute(MixedMetricMethodClass.class);

        Object obj = clazz.newInstance();

        obj.getClass().getMethod("timed").invoke(obj);

        long[] values = metrics.getTimes("timed", new String[]{"timed"});
        assertEquals(1, values.length);
        assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));

        assertEquals(1, metrics.getCount("timed", new String[]{"counted"}));
        assertEquals(0, metrics.getCount("timed", new String[]{"exception"}));
    }

    @Test
    public void shouldRecordMethodInvocationWhenExceptionThrownStatistics() throws Exception {
        Class<MixedMetricMethodClassWithException> clazz = execute(MixedMetricMethodClassWithException.class);

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
        assertEquals(1, metrics.getCount("timed", new String[]{"counted"}));
    }

    public static class MixedMetricConstructorClass {

        @Timed(name = "constructor", labels = {"type:timed"})
        @ExceptionCounted(name = "constructor", labels = {"type:exception"})
        @Counted(name = "constructor", labels = {"type:counted"})
        public MixedMetricConstructorClass() {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
            }
        }
    }

    public static class MixedMetricMethodClass {

        @Timed(name = "timed", labels = {"type:timed"})
        @ExceptionCounted(name = "timed", labels = {"type:exception"})
        @Counted(name = "timed", labels = {"type:counted"})
        public void timed() {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
            }
        }
    }

    public static class MixedMetricMethodClassWithException {

        @Timed(name = "timed", labels = {"type:timed"})
        @ExceptionCounted(name = "timed", labels = {"type:exception"})
        @Counted(name = "timed", labels = {"type:counted"})
        public void timed() {
            try {
                Thread.sleep(10L);
                throw new RuntimeException();
            }
            catch (InterruptedException e) {
            }
        }
    }
}
