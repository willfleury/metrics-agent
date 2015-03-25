package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class TimerInjector extends AbstractInjector {

    private static final String METHOD = "recordTime";
    private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;J)V";

    private final Metric metric;
    
    private int startTimeVar;
    private Label startFinally;

    public TimerInjector(Metric metric, AdviceAdapter aa, Type[] argTypes, int access) {
        super(aa, argTypes, access);
        this.metric = metric;
    }

    @Override
    public void injectAtMethodEnter() {
        startFinally = new Label();
        startTimeVar = aa.newLocal(Type.LONG_TYPE);
        aa.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        aa.visitVarInsn(LSTORE, startTimeVar);
        aa.visitLabel(startFinally);
    }

    @Override
    public void injectAtVisitMaxs(int maxStack, int maxLocals) {
        Label endFinally = new Label();
        aa.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
        aa.visitLabel(endFinally);
        
        onFinally(ATHROW);
        aa.visitInsn(ATHROW);
    }

    @Override
    public void injectAtMethodExit(int opcode) {
        if (opcode != ATHROW) {
            onFinally(opcode);
        }
    }

    private void onFinally(int opcode) {
        injectNameAndLabelToStack(metric);

        aa.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        aa.visitVarInsn(LLOAD, startTimeVar);
        aa.visitInsn(LSUB);
        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
    }
}
