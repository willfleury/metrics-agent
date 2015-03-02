package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class CounterInjector extends AbstractInjector {

    private static final String METHOD = "recordCount";
    private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;)V";

    public CounterInjector(Metric metric, AdviceAdapter aa, MethodVisitor mv, Type[] argTypes) {
        super(metric, aa, mv, argTypes);
    }

    @Override
    public void injectAtMethodEnter() {
        injectNameAndLabelToStack();
        mv.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
    }

}
