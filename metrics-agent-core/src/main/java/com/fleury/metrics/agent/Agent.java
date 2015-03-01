package com.fleury.metrics.agent;

import com.fleury.metrics.agent.config.Args;
import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.transformer.AnnotatedMetricClassTransformer;
import java.lang.instrument.Instrumentation;

/**
 *
 * @author Will Fleury
 */
public class Agent {

	public static void premain(String args, Instrumentation instrumentation) {
		Configuration config = Configuration.createConfig(new Args(args).getConfigFilename());

		instrumentation.addTransformer(new AnnotatedMetricClassTransformer(config));
	}
}
