package com.fleury.metrics.agent.transformer.visitors.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Transforms from
 *
 * <pre>
 * public void someMethod() {
 *     //original method code
 * }
 * </pre>
 *
 * To
 *
 * <pre>
 * public void someMethod() {
 *     long startTimer = System.nanoTime();
 *     try {
 *
 *         //original method code
 *
 *     } finally {
 *         PrometheusMetricSystem.recordTime(TIMER, labels);
 *     }
 * }
 * </pre>
 *
 * @author Will Fleury
 */
public class TimerInjector extends AbstractInjector {

    private static final String METHOD = "recordTime";
    private static final String SIGNATURE = Type.getMethodDescriptor(
            Type.VOID_TYPE,
            Type.getType(String.class), Type.getType(String[].class), Type.LONG_TYPE);

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
