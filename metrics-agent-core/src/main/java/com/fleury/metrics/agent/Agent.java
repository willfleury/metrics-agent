package com.fleury.metrics.agent;

import static com.fleury.metrics.agent.config.LoggerUtil.initializeLogging;

import com.fleury.metrics.agent.config.ArgParser;
import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.reporter.MetricSystemProviderFactory;
import com.fleury.metrics.agent.transformer.AnnotatedMetricClassTransformer;
import java.lang.instrument.Instrumentation;

/**
 *
 * @author Will Fleury
 */
public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {

        ArgParser argParser = new ArgParser(args);

        initializeLogging(argParser.getLogConfigFilename());

        Configuration config = Configuration.createConfig(argParser.getConfigFilename());
        MetricSystemProviderFactory.INSTANCE.init(config.getSystem());

        instrumentation.addTransformer(
                new AnnotatedMetricClassTransformer(config),
                instrumentation.isRetransformClassesSupported());
    }
}
