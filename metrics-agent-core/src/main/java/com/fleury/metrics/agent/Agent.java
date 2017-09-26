package com.fleury.metrics.agent;

import com.fleury.metrics.agent.config.ArgParser;
import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.reporter.MetricSystemProviderFactory;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.transformer.AnnotatedMetricClassTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.LogManager;

/**
 *
 * @author Will Fleury
 */
public class Agent {

    public static void premain(String args, Instrumentation instrumentation) {

        ArgParser argParser = new ArgParser(args);

        initializeLogging(argParser.getLogConfigFilename());

        Configuration config = Configuration.createConfig(argParser.getConfigFilename());
        MetricSystemProviderFactory.INSTANCE.init(config.getMetricSystemConfiguration());

        instrumentation.addTransformer(
                new AnnotatedMetricClassTransformer(config),
                instrumentation.isRetransformClassesSupported());

        startDefaultMetricEndpoint();
    }

    private static void initializeLogging(String resource) {
        try {
            LogManager.getLogManager().readConfiguration(
                    Agent.class.getResourceAsStream(resource));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize agent logging with config: " + resource);
        }
    }

    private static void startDefaultMetricEndpoint() {
        //start in background thread so we don't delay application startup
        new Thread(new Runnable() {
            @Override
            public void run() {
                Reporter.startDefaultMetricEndpoint();
            }
        }).start();
    }
}
