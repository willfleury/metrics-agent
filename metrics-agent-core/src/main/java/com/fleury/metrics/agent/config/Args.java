package com.fleury.metrics.agent.config;

/**
 *
 * @author Will Fleury
 */
public class Args {

    private final String agentArgs;

    public Args(String agentArgs) {
        this.agentArgs = agentArgs;
    }

    public String getConfigFilename() {
        if (agentArgs == null || agentArgs.isEmpty()) {
            return System.getProperty("agent-config");
        }

        return agentArgs.substring(agentArgs.indexOf(':') + 1);
    }
}
