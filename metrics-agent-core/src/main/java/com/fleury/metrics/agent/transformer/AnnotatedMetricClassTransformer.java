package com.fleury.metrics.agent.transformer;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.transformer.asm.MetricClassVisitor;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Will Fleury
 */
public class AnnotatedMetricClassTransformer implements ClassFileTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedMetricClassTransformer.class);
    private final Configuration config;

    public AnnotatedMetricClassTransformer(Configuration config) {
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        if (config.inWhiteList(className)) {
            LOGGER.debug("Transforming class: {}", className);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            ClassVisitor cv = new MetricClassVisitor(config, cw);

            cr.accept(cv, ClassReader.EXPAND_FRAMES);

            return cw.toByteArray();
        }

        return classfileBuffer;
    }

}
