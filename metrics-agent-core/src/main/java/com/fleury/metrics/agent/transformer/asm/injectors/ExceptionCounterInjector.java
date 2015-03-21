package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class ExceptionCounterInjector extends AbstractInjector {

    private static final String METHOD = "recordCount";
    private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;)V";
    
    private final Metric metric;
    
    private Label tryBlockStart;
    private Label tryBlockEnd;
    private Label catchBlockStart;
    private Label catchBlockEnd;

    public ExceptionCounterInjector(Metric metric, AdviceAdapter aa, Type[] argTypes) {
        super(aa, argTypes);
        this.metric = metric;
    }
    
    @Override
    public void injectAtMethodEnter() {
        tryBlockStart = new Label();
        tryBlockEnd = new Label();
        catchBlockStart = new Label();
        catchBlockEnd = new Label();

        aa.visitTryCatchBlock(tryBlockStart, tryBlockEnd, catchBlockStart, "java/lang/Exception");
        aa.visitLabel(tryBlockStart);
    }

    @Override
    public void injectAtVisitMaxs(int maxStack, int maxLocals) {
        aa.visitLabel(tryBlockEnd);
        aa.visitJumpInsn(GOTO, catchBlockEnd);
        
        aa.visitLabel(catchBlockStart);

        int exVar = aa.newLocal(Type.getType(RuntimeException.class));
        aa.visitVarInsn(ASTORE, exVar);
        
        injectNameAndLabelToStack(metric);

        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
        
        aa.visitVarInsn(ALOAD, exVar);
        aa.visitInsn(ATHROW);
        
        aa.visitLabel(catchBlockEnd);
    }
}
