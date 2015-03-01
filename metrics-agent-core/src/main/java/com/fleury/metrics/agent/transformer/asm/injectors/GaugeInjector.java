package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class GaugeInjector extends AbstractInjector {
	
	private static final String INC_METHOD = "recordGaugeInc";
	private static final String DEC_METHOD = "recordGaugeDec";
	private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;)V";
	
	
	public GaugeInjector(Metric metric, AdviceAdapter aa, MethodVisitor mv, Type[] argTypes) {
		super(metric, aa, mv, argTypes);
	}
	
	@Override
	public void injectAtMethodEnter() {
		injectNameAndLabelToStack();
		
		String method = getMethod();
		mv.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, method, SIGNATURE, false);
	}

	private String getMethod() {
		return "inc".equals(metric.getExt().get("mode")) 
				? INC_METHOD : DEC_METHOD;
	}
	
}
