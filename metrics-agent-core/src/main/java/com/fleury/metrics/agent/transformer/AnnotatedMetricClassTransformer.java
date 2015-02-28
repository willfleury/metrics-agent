package com.fleury.metrics.agent.transformer;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.transformer.asm.MetricClassVisitor;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class AnnotatedMetricClassTransformer implements ClassFileTransformer {
	
	private final Configuration config;
	
	public AnnotatedMetricClassTransformer(Configuration config) {
		this.config = config;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		ClassVisitor cv = new MetricClassVisitor(config, cw);

		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		return cw.toByteArray();
	}

}
