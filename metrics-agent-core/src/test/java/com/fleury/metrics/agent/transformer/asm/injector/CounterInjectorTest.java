package com.fleury.metrics.agent.transformer.asm.injector;


import com.fleury.metrics.agent.annotation.Counted;
import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.objectweb.asm.Type;

/**
 *
 * @author Will Fleury
 */
public class CounterInjectorTest extends BaseMetricTest {

	@Test
	public void shouldCountConstructorInvocation() throws Exception {
		Class<CountedConstructorClass> clazz = execute(CountedConstructorClass.class);
		
		Object obj = clazz.newInstance();
		
		assertEquals(1, metrics.getCount("constructor"));
	}
	
	
	@Test
	public void shouldCountConstructorInvocationWithConfiguration() throws Exception {
		Metric meta = new Metric(MetricType.Counted);
		meta.setName("constructor");
		meta.setLabels(Arrays.asList("label1:value1"));
		
		Configuration.Key key = new Configuration.Key(
				Type.getType(ConfigurationCountedConstructorClass.class).getInternalName(), 
				"<init>()V");
		Map<Configuration.Key, List<Metric>> mConfig = new HashMap<Configuration.Key, List<Metric>>();
		mConfig.put(key, Arrays.asList(meta));
		
		Configuration config = new Configuration(mConfig);
		
		Class<ConfigurationCountedConstructorClass> clazz = execute(ConfigurationCountedConstructorClass.class, config);
		
		Object obj = clazz.newInstance();
		
		assertEquals(1, metrics.getCount("constructor", new String[] {"value1"}));
	}
	
	
	@Test
	public void shouldCountMethodInvocation() throws Exception {
		Class<CountedMethodClass> clazz = execute(CountedMethodClass.class);
		
		Object obj = clazz.newInstance();
		
		obj.getClass().getMethod("counted").invoke(obj);
		
		assertEquals(1, metrics.getCount("counted"));
	}
	
	@Test
	public void shouldCountMethodWithLabelsInvocation() throws Exception {
		Class<CountedMethodWithLabelsClass> clazz = execute(CountedMethodWithLabelsClass.class);
		
		Object obj = clazz.newInstance();
		
		obj.getClass().getMethod("counted").invoke(obj);
		
		assertEquals(1, metrics.getCount("counted", new String[] {"value1"}));
	}
	
	@Test
	public void shouldCountMethodWithParametersAndReturnInvocation() throws Exception {
		Class<CountedMethodWithParametersAndReturnClass> clazz = execute(CountedMethodWithParametersAndReturnClass.class);
		
		Object obj = clazz.newInstance();
		
		Long count = (Long)obj.getClass().getMethod("counted", int.class).invoke(obj, 5);
		
		assertEquals(1, metrics.getCount("counted"));
		assertTrue(count >= 5); 
	}
	
	public static class CountedConstructorClass {
		
		@Counted(name = "constructor")
		public CountedConstructorClass() {
			BaseMetricTest.performBasicTask();
		}
	}
	
	public static class CountedMethodClass {
		
		@Counted(name = "counted")
		public void counted() {
			BaseMetricTest.performBasicTask();
		}
	}
	
	public static class CountedMethodWithLabelsClass {
		
		@Counted(name = "counted", labels = "label1:value1")
		public void counted() {
			BaseMetricTest.performBasicTask();
		}
	}
	
	public static class CountedMethodWithParametersAndReturnClass {
		
		@Counted(name = "counted")
		public long counted(int var) {
			long x = (long)(Math.random() * 10);
			BaseMetricTest.performBasicTask();
			return x + var;
		}
	}
	
	public static class ConfigurationCountedConstructorClass {
		
		public ConfigurationCountedConstructorClass() {
			BaseMetricTest.performBasicTask();
		}
	}

}
