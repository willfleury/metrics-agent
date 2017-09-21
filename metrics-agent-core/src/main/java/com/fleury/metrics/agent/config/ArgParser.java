package com.fleury.metrics.agent.config;

/**
 *
 * @author Will Fleury
 */
public class ArgParser {

    private final String[] agentArgs;

    public ArgParser(String args) {
        this.agentArgs = args == null ? new String[] {} : args.split(",");
    }

    public String getConfigFilename() {
        if (agentArgs.length == 0) {
            return System.getProperty("agent-config");
        }

        return getArg("agent-config");
    }

    public String getLogConfigFilename() {
        if (agentArgs.length == 0) {
            return System.getProperty("log-config");
        }

        String resource = getArg("log-config");

        return resource == null ? "/logging.properties" : resource;
    }

    public String getArg(String key) {
        for (String arg : agentArgs) {
            if (arg.startsWith(key)) {
                return arg.replace(key + ":", "");
            }
        }

        return null;
    }
}
