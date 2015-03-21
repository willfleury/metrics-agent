package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.LabelUtil;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.transformer.asm.util.OpCodeUtil;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public abstract class AbstractInjector implements Injector, Opcodes {

    public static final String METRIC_REPORTER_CLASSNAME = Type.getType(Reporter.class).getInternalName();

    protected final AdviceAdapter aa;
    protected final Type[] argTypes;

    public AbstractInjector(AdviceAdapter aa, Type[] argTypes) {
        this.aa = aa;
        this.argTypes = argTypes;
    }

    @Override
    public void injectAtMethodEnter() {
    }

    @Override
    public void injectAtVisitMaxs(int maxStack, int maxLocals) {
    }

    @Override
    public void injectAtMethodExit(int opcode) {
    }

    protected void injectNameAndLabelToStack(Metric metric) {
        int nameVar = aa.newLocal(Type.getType(String.class));
        aa.visitLdcInsn(metric.getName());
        aa.visitVarInsn(ASTORE, nameVar);

        List<String> labelValues = LabelUtil.getLabelValues(metric.getLabels());

        if (labelValues != null && !labelValues.isEmpty()) {
            int labelVar = injectLabelValuesArrayToStack(metric, labelValues);

            aa.visitVarInsn(ALOAD, nameVar);
            aa.visitVarInsn(ALOAD, labelVar);
        } else {
            aa.visitVarInsn(ALOAD, nameVar);
            aa.visitInsn(ACONST_NULL);
        }
    }

    protected int injectLabelValuesArrayToStack(Metric metric, List<String> labelValues) {
        if (labelValues.size() > 5) {
            throw new IllegalStateException("Maximum labels per metric is 5. "
                    + metric.getName() + " has " + labelValues.size());
        }
        int labelVar = aa.newLocal(Type.getType(String[].class));

        aa.visitInsn(OpCodeUtil.getIConstOpcodeForInteger(labelValues.size()));
        aa.visitTypeInsn(ANEWARRAY, Type.getType(String.class).getInternalName());

        for (int i = 0; i < labelValues.size(); i++) {
            aa.visitInsn(DUP);
            aa.visitInsn(OpCodeUtil.getIConstOpcodeForInteger(i));
            injectLabelValueToStack(labelValues.get(i));
        }

        aa.visitVarInsn(ASTORE, labelVar);

        return labelVar;
    }

    private void injectLabelValueToStack(String labelValue) {
        if (!labelValue.startsWith("$")) {
            aa.visitLdcInsn(labelValue);
        } 
        else {
            int var = LabelUtil.getLabelValueVarIndex(labelValue);

            pushMethodParameterValueAsString(argTypes[var - 1], var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf",
                    "(Ljava/lang/Object;)Ljava/lang/String;", false);
        }

        aa.visitInsn(AASTORE);
    }

    private void pushMethodParameterValueAsString(Type type, int var) {
        if (type.equals(Type.BOOLEAN_TYPE)) {
            aa.visitVarInsn(ILOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } 
        else if (type.equals(Type.BYTE_TYPE)) {
            aa.visitVarInsn(ILOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } 
        else if (type.equals(Type.CHAR_TYPE)) {
            aa.visitVarInsn(ILOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        } else if (type.equals(Type.SHORT_TYPE)) {
            aa.visitVarInsn(ILOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } 
        else if (type.equals(Type.INT_TYPE)) {
            aa.visitVarInsn(ILOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } 
        else if (type.equals(Type.LONG_TYPE)) {
            aa.visitVarInsn(LLOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        } 
        else if (type.equals(Type.FLOAT_TYPE)) {
            aa.visitVarInsn(FLOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } 
        else if (type.equals(Type.DOUBLE_TYPE)) {
            aa.visitVarInsn(DLOAD, var);
            aa.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        } 
        else {
            aa.visitVarInsn(ALOAD, var);
        }
    }
}
