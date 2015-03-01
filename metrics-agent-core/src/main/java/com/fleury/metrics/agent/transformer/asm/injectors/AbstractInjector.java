package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.LabelUtil;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.transformer.asm.util.OpCodeUtil;
import java.util.List;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LLOAD;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public abstract class AbstractInjector implements Injector {
	
	public static final String METRIC_REPORTER_CLASSNAME = Type.getType(Reporter.class).getInternalName();
	
	protected final AdviceAdapter aa;
	protected final MethodVisitor mv;
	protected final Type[] argTypes;
	protected final Metric metric;
	
	public AbstractInjector(Metric metric, AdviceAdapter aa, MethodVisitor mv, Type[] argTypes) {
		this.metric = metric;
		this.aa = aa;
		this.mv = mv;
		this.argTypes = argTypes;
	}
	
	@Override
	public void injectAtMethodEnter() {}
	
	@Override
	public void injectAtVisitMaxs(int maxStack, int maxLocals) {}
	
	@Override
	public void injectAtMethodExit(int opcode) { }
	
	protected void injectNameAndLabelToStack() {
		int nameVar = aa.newLocal(Type.getType(String.class));
		mv.visitLdcInsn(metric.getName());
		mv.visitVarInsn(ASTORE, nameVar);

		List<String> labelValues = LabelUtil.getLabelValues(metric.getLabels());

		if (labelValues != null && !labelValues.isEmpty()) {
			int labelVar = injectLabelValuesArrayToStack(labelValues);
			
			mv.visitVarInsn(ALOAD, nameVar);
			mv.visitVarInsn(ALOAD, labelVar);
		} 
		else {
			mv.visitVarInsn(ALOAD, nameVar);
			mv.visitInsn(ACONST_NULL);
		}
	}
	
	protected int injectLabelValuesArrayToStack(List<String> labelValues)  {
		if (labelValues.size() > 5) {
			throw new IllegalStateException("Maximum labels per metric is 5. "
					+ metric.getName() + " has " + labelValues.size());
		}
		int labelVar = aa.newLocal(Type.getType(String[].class));
		
		mv.visitInsn(OpCodeUtil.getIConstOpcodeForInteger(labelValues.size())); 
		mv.visitTypeInsn(ANEWARRAY, Type.getType(String.class).getInternalName());
		
		for (int i = 0; i < labelValues.size(); i++) {
			mv.visitInsn(DUP);
			mv.visitInsn(OpCodeUtil.getIConstOpcodeForInteger(i)); 
			injectLabelValueToStack(labelValues.get(i));
		}
		
		mv.visitVarInsn(ASTORE, labelVar);
		
		return labelVar;
	}
	
	private void injectLabelValueToStack(String labelValue) {
		if (!labelValue.startsWith("$")) {
			mv.visitLdcInsn(labelValue); 
		} 
		else {
			int var = LabelUtil.getLabelValueVarIndex(labelValue);
			
			pushMethodParameterValueAsString(argTypes[var - 1], var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", 
					"(Ljava/lang/Object;)Ljava/lang/String;", false);
		}
		
		mv.visitInsn(AASTORE); 
	}
	
	private void pushMethodParameterValueAsString(Type type, int var) {
		if (type.equals(Type.BOOLEAN_TYPE)) {
			mv.visitVarInsn(ILOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
		}
		else if (type.equals(Type.BYTE_TYPE)) {
			mv.visitVarInsn(ILOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
		}
		else if (type.equals(Type.CHAR_TYPE)) {
			mv.visitVarInsn(ILOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
		}
		else if (type.equals(Type.SHORT_TYPE)) {
			mv.visitVarInsn(ILOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
		}
		else if (type.equals(Type.INT_TYPE)) {
			mv.visitVarInsn(ILOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else if (type.equals(Type.LONG_TYPE)) {
			mv.visitVarInsn(LLOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
		}
		else if (type.equals(Type.FLOAT_TYPE)) {
			mv.visitVarInsn(FLOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
		}
		else if (type.equals(Type.DOUBLE_TYPE)) {
			mv.visitVarInsn(DLOAD, var);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
		}
		else {
			mv.visitVarInsn(ALOAD, var);
		}
	}
}
