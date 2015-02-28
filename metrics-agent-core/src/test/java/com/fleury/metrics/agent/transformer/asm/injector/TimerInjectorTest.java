package com.fleury.metrics.agent.transformer.asm.injector;

import com.fleury.metrics.agent.annotation.Timed;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class TimerInjectorTest extends BaseMetricTest {
	
	@Test
	public void shouldTimeConstructorInvocation() throws Exception {
		Class<TimedConstructorClass> clazz = execute(TimedConstructorClass.class);
		
		Object obj = clazz.newInstance();

		long[] values = metrics.getTimes("constructor");
		assertEquals(1, values.length);
		assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));
	}
	
	@Test
	public void shouldTimeMethodInvocation() throws Exception {
		Class<TimedMethodClass> clazz = execute(TimedMethodClass.class);
		
		Object obj = clazz.newInstance();
		
		obj.getClass().getMethod("timed").invoke(obj);
		
		long[] values = metrics.getTimes("timed");
		assertEquals(1, values.length);
		assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));
	}
	
	@Test
	public void shouldTimeMethodWithLabelsInvocation() throws Exception {
		Class<TimedMethodWithLabelsClass> clazz = execute(TimedMethodWithLabelsClass.class);
		
		Object obj = clazz.newInstance();
		
		obj.getClass().getMethod("timed").invoke(obj);
		
		long[] values = metrics.getTimes("timed", new String[] {"value1"});
		assertEquals(1, values.length);
		assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));
	}
	
	@Test
	public void shouldTimeMethodInvocationWhenExceptionThrown() throws Exception {
		Class<TimedMethodClassWithException> clazz = execute(TimedMethodClassWithException.class);
		
		Object obj = clazz.newInstance();
		
		boolean exceptionOccured = false;
		try {
			obj.getClass().getMethod("timed").invoke(obj);
		} catch (InvocationTargetException e) {
			exceptionOccured = true;
		}
		
		assertTrue(exceptionOccured);
		
		long[] values = metrics.getTimes("timed");
		assertEquals(1, values.length);
		assertTrue(values[0] >= TimeUnit.NANOSECONDS.toMillis(10L));
	}
	
	
	public static class TimedConstructorClass {
		
		@Timed(name = "constructor")
		public TimedConstructorClass() {
			try { 
				Thread.sleep(10L); 
			} catch (InterruptedException e) { }
		}
	}

	public static class TimedMethodClass {
		
		@Timed(name = "timed")
		public void timed() {
			try { 
				Thread.sleep(10L); 
			} catch (InterruptedException e) { }
		}
	}
	
	public static class TimedMethodWithLabelsClass {
		
		@Timed(name = "timed", labels = {"name1:value1"})
		public void timed() {
			try { 
				Thread.sleep(10L); 
			} catch (InterruptedException e) { }
		}
	}
	
	
	public static class TimedMethodClassWithException {
		
		@Timed(name = "timed")
		public void timed() {
			try { 
				Thread.sleep(10L); 
				throw new RuntimeException();
			} catch (InterruptedException e) { }
		}
	}
}
