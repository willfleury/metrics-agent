package com.fleury.metrics.agent.config;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
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
		
		return agentArgs.split(":")[1];
	}
}
