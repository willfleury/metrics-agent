package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Label;
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
    
    private Label lTryBlockStart;
    private Label lTryBlockEnd;
    private Label lCatchBlockStart;
    private Label lCatchBlockEnd;

    public ExceptionCounterInjector(Metric metric, AdviceAdapter aa, MethodVisitor mv, Type[] argTypes) {
        super(metric, aa, mv, argTypes);
    }
    
    @Override
    public void injectAtMethodEnter() {
        lTryBlockStart = new Label();
        lTryBlockEnd = new Label();
        lCatchBlockStart = new Label();
        lCatchBlockEnd = new Label();

        aa.visitTryCatchBlock(lTryBlockStart, lTryBlockEnd, lCatchBlockStart, "java/lang/Exception");
        aa.visitLabel(lTryBlockStart);
    }

    @Override
    public void injectAtVisitMaxs(int maxStack, int maxLocals) {
        aa.visitLabel(lTryBlockEnd);
        aa.visitJumpInsn(GOTO, lCatchBlockEnd);
        
        aa.visitLabel(lCatchBlockStart);

        int exVar = aa.newLocal(Type.getType(RuntimeException.class));
        aa.visitVarInsn(ASTORE, exVar);
        
        injectNameAndLabelToStack();

        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
        
        aa.visitVarInsn(ALOAD, exVar);
        aa.visitInsn(ATHROW);
        
        aa.visitLabel(lCatchBlockEnd);
    }
    

}
