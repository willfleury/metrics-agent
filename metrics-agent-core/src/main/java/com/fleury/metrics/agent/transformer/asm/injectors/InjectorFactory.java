package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class InjectorFactory {
	
	public static Injector createInjector(Metric metric, 
			AdviceAdapter adviceAdapter, MethodVisitor mv, Type[] argTypes) {
		switch (metric.getType()) {
			case Counted: 
				return new CounterInjector(metric, adviceAdapter, mv, argTypes);
				
			case Gauged: 
				return new GaugeInjector(metric, adviceAdapter, mv, argTypes);
				
			case ExceptionCounted: 
				return new ExceptionCounterInjector(metric, adviceAdapter, mv, argTypes);
				
			case Timed: 
				return new TimerInjector(metric, adviceAdapter, mv, argTypes);
				
			default: 
				throw new IllegalStateException("unknown metric type: " + metric.getType());
		}
	}

}
