package com.fleury.metrics.agent.transformer.asm.injector;

import com.fleury.metrics.agent.annotation.Counted;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class LabelsTest extends BaseMetricTest {
	
	@Test
	public void shouldCountConstructorInvocationWithLabels() throws Exception {
		testInvocation(CountedConstructorWithLabelsClass.class, new String[] {"value1", "value2"});
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
				new Object[] {"hello"}, new String[] {"hello"});
	}
	
	@Test
	public void shouldCountConstructorInvocationWithDynamicLongValue() throws Exception {
		testInvocationWithArgs(CountedConstructorWithDynamicLongLabelValueClass.class, 
				new Object[] {5}, new String[] {"5"});
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
	
	public static class CountedConstructorWithDynamicStringLabelValueClass {
		
		@Counted(name = "constructor", labels = {"name1:$1"})
		public CountedConstructorWithDynamicStringLabelValueClass(String value) {
			BaseMetricTest.performBasicTask();
		}
	}
	
	public static class CountedConstructorWithDynamicLongLabelValueClass {
		
		@Counted(name = "constructor", labels = {"name1:$1"})
		public CountedConstructorWithDynamicLongLabelValueClass(long value) {
			BaseMetricTest.performBasicTask();
		}
	}
}
