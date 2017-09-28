package com.fleury.metrics.agent.config;


import com.fleury.metrics.agent.Agent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class LoggerUtil {

    public static void initializeLogging(String resource) {
        InputStream in = null;
        try {
            in = Agent.class.getResourceAsStream(resource);

            if (in == null) {
                throw new NullPointerException("Logger configuration " + resource + " not found");
            }

            LogManager.getLogManager().readConfiguration(in);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize agent logging with config: " + resource, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) { }
            }
        }
    }
}

