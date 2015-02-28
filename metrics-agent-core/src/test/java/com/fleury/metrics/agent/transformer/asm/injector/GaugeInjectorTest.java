package com.fleury.metrics.agent.transformer.asm.injector;


import com.fleury.metrics.agent.annotation.Gauged;
import com.fleury.metrics.agent.annotation.Gauged.mode;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class GaugeInjectorTest extends BaseMetricTest {

	@Test
	public void shouldIncrementGaugedConstructorInvocation() throws Exception {
		Class<GaugedConstructorClass> clazz = execute(GaugedConstructorClass.class);
		
		Object obj = clazz.newInstance();
		
		assertEquals(1, metrics.getCount("constructor"));
	}
	
	
	@Test
	public void shouldIncrementAndDecrementOnMethodInvocation() throws Exception {
		Class<GaugedMethodClass> clazz = execute(GaugedMethodClass.class);
		
		Object obj = clazz.newInstance();
		
		obj.getClass().getMethod("incrementOnMethod").invoke(obj);
		
		assertEquals(1, metrics.getCount("request_handler"));
		
		obj.getClass().getMethod("decrementOnMethod").invoke(obj);
		
		assertEquals(0, metrics.getCount("request_handler"));
	}

	
	public static class GaugedConstructorClass {
		
		@Gauged(name = "constructor", mode = mode.inc)
		public GaugedConstructorClass() {
			BaseMetricTest.performBasicTask();
		}
	}
	
	public static class GaugedMethodClass {
		
		@Gauged(name = "request_handler", mode = mode.inc)
		public void incrementOnMethod() {
			BaseMetricTest.performBasicTask();
		}
		
		@Gauged(name = "request_handler", mode = mode.dec)
		public void decrementOnMethod() {
			BaseMetricTest.performBasicTask();
		}
	}

}
