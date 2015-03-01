package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.Metric;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

/**
 *
 * @author Will Fleury
 */
public class MetricClassVisitor extends ClassVisitor {
	
	private boolean isInterface;
	private String className;
	private final Configuration config;

	public MetricClassVisitor(Configuration config, ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
		this.config = config;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		cv.visit(version, access, name, signature, superName, interfaces);
		this.className = name;
		this.isInterface = (access & ACC_INTERFACE) != 0;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		if (!isInterface && mv != null) { 
			List<Metric> metadata = config.findMetrics(className, name + desc);
			mv = new MetricAdapter(metadata, mv, access, name, desc);
		}

		return mv;
	}
}
