package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class TimerInjector extends AbstractInjector {
	
	private static final String METHOD = "recordTime";
	private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;J)V";
	
	
	private int startTimeVar;
	private Label startFinally;
	
	public TimerInjector(Metric metric, AdviceAdapter aa, MethodVisitor mv, Type[] argTypes) {
		super(metric, aa, mv, argTypes);
	}
	
	@Override
	public void injectAtMethodEnter() {
		startFinally = new Label();
		startTimeVar = aa.newLocal(Type.LONG_TYPE);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
		mv.visitVarInsn(LSTORE, startTimeVar);
		mv.visitLabel(startFinally);
	}
	

	@Override
	public void injectAtVisitMaxs(int maxStack, int maxLocals) {
		Label endFinally = new Label();
		mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
		mv.visitLabel(endFinally);
		onFinally(ATHROW);
		mv.visitInsn(ATHROW);
	}
	
	@Override
	public void injectAtMethodExit(int opcode) {
		if (opcode != ATHROW) {
			onFinally(opcode);
		} 
	}
	
	private void onFinally(int opcode) {
		injectNameAndLabelToStack();
		
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
		mv.visitVarInsn(LLOAD, startTimeVar);
		mv.visitInsn(LSUB);
		mv.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, METHOD, SIGNATURE, false);
	}
}
