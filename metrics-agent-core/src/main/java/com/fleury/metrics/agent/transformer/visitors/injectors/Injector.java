package com.fleury.metrics.agent.transformer.visitors.injectors;

/**
 *
 * @author Will Fleury
 */
public interface Injector {

    public void injectAtMethodEnter();

    public void injectAtVisitMaxs(int maxStack, int maxLocals);

    public void injectAtMethodExit(int opcode);
}
