package com.fleury.metrics.agent.transformer.asm.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fleury.metrics.agent.annotation.Gauged;
import com.fleury.metrics.agent.annotation.Gauged.mode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class GaugeInjectorTest extends BaseMetricTest {

    @Test
    public void shouldMeasureConstructorInFlight() throws Exception {
        final Class<GaugedConstructorClass> clazz = execute(GaugedConstructorClass.class);

        final CountDownLatch inProgressLatch = new CountDownLatch(1);
        final CountDownLatch callFinishedLatch = new CountDownLatch(1);

        startInNewThread(new Runnable() {
            @Override
            public void run() {
                try {
                    clazz.getConstructor(CountDownLatch.class).newInstance(inProgressLatch);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    callFinishedLatch.countDown();
                }
            }
        });

        Thread.sleep(500); //wait to init thread creating new instance above

        assertEquals(1, metrics.getCount("constructor_initializing"));

        //allow request to finish
        inProgressLatch.countDown();

        callFinishedLatch.await();

        //constructor has finished - so no in flight
        assertEquals(0, metrics.getCount("constructor_initializing"));
    }

    @Test
    public void shouldMeasureConstructorInFlightWithException() throws Exception {
        final Class<GaugedConstructorExceptionClass> clazz = execute(GaugedConstructorExceptionClass.class);

        final CountDownLatch inProgressLatch = new CountDownLatch(1);
        final CountDownLatch callFinishedLatch = new CountDownLatch(1);
        final AtomicBoolean exceptionOccurred = new AtomicBoolean(false);

        startInNewThread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        clazz.getConstructor(CountDownLatch.class).newInstance(inProgressLatch);
                    } catch (Exception e) {
                        if (e.getCause().getMessage().equals("Something bad..")) {
                            exceptionOccurred.set(true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    callFinishedLatch.countDown();
                }
            }
        });

        Thread.sleep(500); //wait to init thread creating new instance above

        assertEquals(1, metrics.getCount("constructor_initializing"));

        //allow request to finish
        inProgressLatch.countDown();

        callFinishedLatch.await();

        //constructor has finished - so not in flight
        assertEquals(0, metrics.getCount("constructor_initializing"));
        assertTrue(exceptionOccurred.get());
    }

    @Test
    public void shouldMeasureMethodInFlight() throws Exception {
        final Class<GaugedMethodClass> clazz = execute(GaugedMethodClass.class);

        final CountDownLatch inProgressLatch = new CountDownLatch(1);
        final CountDownLatch callFinishedLatch = new CountDownLatch(1);

        startInNewThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Object obj = clazz.newInstance();
                    obj.getClass().getMethod("handleRequest", CountDownLatch.class).invoke(obj, inProgressLatch);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    callFinishedLatch.countDown();
                }
            }
        });

        Thread.sleep(500); //wait for init above and request method call

        assertEquals(1, metrics.getCount("request_handler"));

        //allow request to finish
        inProgressLatch.countDown();

        callFinishedLatch.await();

        //request has finished - so no in flight
        assertEquals(0, metrics.getCount("constructor_initializing"));
    }

    public static class GaugedConstructorClass {

        @Gauged(name = "constructor_initializing", mode = mode.in_flight)
        public GaugedConstructorClass(CountDownLatch latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class GaugedConstructorExceptionClass {

        @Gauged(name = "constructor_initializing", mode = mode.in_flight)
        public GaugedConstructorExceptionClass(CountDownLatch latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            throw new RuntimeException("Something bad..");
        }
    }

    public static class GaugedMethodClass {

        @Gauged(name = "request_handler", mode = mode.in_flight)
        public void handleRequest(CountDownLatch latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startInNewThread(Runnable r) {
        new Thread(r).start();
    }

}
