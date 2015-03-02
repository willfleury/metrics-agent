package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class ExceptionCounterInjector extends AbstractInjector {

    private static final String METHOD = "recordCount";
    private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;)V";

    public ExceptionCounterInjector(Metric metric, AdviceAdapter aa, MethodVisitor mv, Type[] argTypes) {
        super(metric, aa, mv, argTypes);
    }

    @Override
    public void injectAtMethodExit(int opcode) {
        if (opcode == ATHROW) {
            injectNameAndLabelToStack();

            mv.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
        }
    }

}
