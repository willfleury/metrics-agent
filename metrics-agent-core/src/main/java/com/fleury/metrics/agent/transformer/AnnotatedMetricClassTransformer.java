package com.fleury.metrics.agent.transformer;

import static java.util.logging.Level.WARNING;
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.transformer.asm.ASMClassWriter;
import com.fleury.metrics.agent.transformer.asm.MetricClassVisitor;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 *
 * @author Will Fleury
 */
public class AnnotatedMetricClassTransformer implements ClassFileTransformer {

    private static final Logger LOGGER = Logger.getLogger(AnnotatedMetricClassTransformer.class.getName());

    private final Configuration config;

    public AnnotatedMetricClassTransformer(Configuration config) {
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ASMClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS, loader);
            ClassVisitor cv = new MetricClassVisitor(cw, config);

            cr.accept(cv, EXPAND_FRAMES);

            return cw.toByteArray();
        } catch (RuntimeException e) {
            LOGGER.log(WARNING, "Unhandled exception", e);
        }

        return classfileBuffer;
    }
}
