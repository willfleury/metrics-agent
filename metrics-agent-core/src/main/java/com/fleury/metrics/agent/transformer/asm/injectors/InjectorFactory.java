package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import static com.fleury.metrics.agent.model.MetricType.Counted;
import static com.fleury.metrics.agent.model.MetricType.ExceptionCounted;
import static com.fleury.metrics.agent.model.MetricType.Timed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class InjectorFactory {
    
    public static List<Injector> createInjectors(Map<MetricType, Metric> metrics, AdviceAdapter adviceAdapter, Type[] argTypes) {
        List<Injector> injectors = new ArrayList<Injector>();
        
        //handle special case for both exception counter and timer (try catch finally)
        if (metrics.containsKey(ExceptionCounted) && metrics.containsKey(Timed)) {
            injectors.add(new TimedExceptionCountedInjector(
                    metrics.get(Timed), 
                    metrics.get(ExceptionCounted), 
                    adviceAdapter, argTypes));
            
            metrics.remove(Timed);
            metrics.remove(ExceptionCounted);
        }
        
        for (Metric metric : metrics.values()) {
            injectors.add(createInjector(metric, adviceAdapter, argTypes));
        }
        
        return injectors;
    }

    public static Injector createInjector(Metric metric, AdviceAdapter adviceAdapter, Type[] argTypes) {
        switch (metric.getType()) {
            case Counted:
                return new CounterInjector(metric, adviceAdapter, argTypes);

            case Gauged:
                return new GaugeInjector(metric, adviceAdapter, argTypes);

            case ExceptionCounted:
                return new ExceptionCounterInjector(metric, adviceAdapter, argTypes);

            case Timed:
                return new TimerInjector(metric, adviceAdapter, argTypes);

            default:
                throw new IllegalStateException("unknown metric type: " + metric.getType());
        }
    }

}
