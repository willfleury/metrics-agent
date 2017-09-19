package com.fleury.metrics.agent;

import com.fleury.metrics.agent.config.Args;
import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.reporter.MetricSystemProviderFactory;
import com.fleury.metrics.agent.transformer.AnnotatedMetricClassTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.LogManager;

/**
 *
 * @author Will Fleury
 */
public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {

        initializeLogging();

        Configuration config = Configuration.createConfig(new Args(args).getConfigFilename());
        MetricSystemProviderFactory.INSTANCE.init(config.getMetricSystemConfiguration());

        instrumentation.addTransformer(
                new AnnotatedMetricClassTransformer(config),
                instrumentation.isRetransformClassesSupported());
    }

    private static void initializeLogging() {
        //TODO allow override of logging configuration file from agent args
        try {
            LogManager.getLogManager().readConfiguration(Agent.class.getResourceAsStream("/logging.properties"));
        } catch (Exception e) {
            System.err.println("Unable to initialize agent logging");
        }
    }
}
