package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Only currently supports IN_FLIGHT mode which means it tracks the number of method calls in flight. To achieve this
 * it increments on method enter and decrements on method exit.
 *
 * @author Will Fleury
 */
public class GaugeInjector extends AbstractInjector {

    private static final String INC_METHOD = "recordGaugeInc";
    private static final String DEC_METHOD = "recordGaugeDec";
    private static final String SIGNATURE = Type.getMethodDescriptor(
            Type.VOID_TYPE,
            Type.getType(String.class), Type.getType(String[].class));

    private final Metric metric;
    
    public GaugeInjector(Metric metric, AdviceAdapter aa, Type[] argTypes, int access) {
        super(aa, argTypes, access);
        this.metric = metric;
    }

    @Override
    public void injectAtMethodEnter() {
        injectNameAndLabelToStack(metric);

        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, INC_METHOD, SIGNATURE, false);
    }

    @Override
    public void injectAtMethodExit(int opcode) {
        injectNameAndLabelToStack(metric);

        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, DEC_METHOD, SIGNATURE, false);
    }

}
