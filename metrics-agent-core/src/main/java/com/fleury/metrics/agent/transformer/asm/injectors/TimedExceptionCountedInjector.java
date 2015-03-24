package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class TimedExceptionCountedInjector extends AbstractInjector {
    
    private static final String EXCEPTION_COUNT_METHOD = "recordCount";
    private static final String EXCEPTION_COUNT_SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;)V";
    
    private static final String TIMER_METHOD = "recordTime";
    private static final String TIMER_SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;J)V";
    
    private final Metric timerMetric;
    private final Metric exceptionMetric;
    
    private int startTimeVar;
    private Label startFinally;
    
    public TimedExceptionCountedInjector(Metric timerMetric, Metric exceptionMetric, AdviceAdapter aa, Type[] argTypes) {
        super(aa, argTypes);
        this.timerMetric = timerMetric;
        this.exceptionMetric = exceptionMetric;
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
        
        injectNameAndLabelToStack(exceptionMetric);
        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, EXCEPTION_COUNT_METHOD, EXCEPTION_COUNT_SIGNATURE, false);
        
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
        injectNameAndLabelToStack(timerMetric);

        aa.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        aa.visitVarInsn(LLOAD, startTimeVar);
        aa.visitInsn(LSUB);
        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, TIMER_METHOD, TIMER_SIGNATURE, false);
    }
}
