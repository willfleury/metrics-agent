package com.fleury.metrics.agent.transformer.asm.injectors;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public interface Injector {	
	public void injectAtMethodEnter();
	public void injectAtVisitMaxs(int maxStack, int maxLocals);
	public void injectAtMethodExit(int opcode);
}
