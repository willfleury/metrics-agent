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
 *     try {
 *
 *         //original method code
 *
 *     } catch (Throwable t) {
 *         PrometheusMetricSystem.recordCount(COUNTER, labels);
 *         throw t;
 *     }
 * }
 * </pre>
 *
 * @author Will Fleury
 */
public class ExceptionCounterInjector extends AbstractInjector {

    private static final String METHOD = "recordCount";
    private static final String SIGNATURE = Type.getMethodDescriptor(
            Type.VOID_TYPE,
            Type.getType(String.class), Type.getType(String[].class));
    
    private final Metric metric;
    
    private Label startFinally;

    public ExceptionCounterInjector(Metric metric, AdviceAdapter aa, Type[] argTypes, int access) {
        super(aa, argTypes, access);
        this.metric = metric;
    }

    @Override
    public void injectAtMethodEnter() {
        startFinally = new Label();
        aa.visitLabel(startFinally);
    }

    @Override
    public void injectAtVisitMaxs(int maxStack, int maxLocals) {
        Label endFinally = new Label();
        aa.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
        aa.visitLabel(endFinally);
        
        injectNameAndLabelToStack(metric);
        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
        
        aa.visitInsn(ATHROW);
    }
}
