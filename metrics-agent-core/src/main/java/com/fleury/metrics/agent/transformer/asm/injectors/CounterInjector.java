package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class CounterInjector extends AbstractInjector {

    private static final String METHOD = "recordCount";
    private static final String SIGNATURE = Type.getMethodDescriptor(
            Type.VOID_TYPE,
            Type.getType(String.class), Type.getType(String[].class));
    
    private final Metric metric;

    public CounterInjector(Metric metric, AdviceAdapter aa, Type[] argTypes, int access) {
        super(aa, argTypes, access);
        this.metric = metric;
    }

    @Override
    public void injectAtMethodEnter() {
        injectNameAndLabelToStack(metric);
        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
    }

}
