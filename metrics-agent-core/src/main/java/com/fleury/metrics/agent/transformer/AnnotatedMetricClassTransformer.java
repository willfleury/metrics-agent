package com.fleury.metrics.agent.transformer;

import static java.util.logging.Level.WARNING;
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.transformer.asm.ASMClassWriter;
import com.fleury.metrics.agent.transformer.asm.AnnotationClassVisitor;
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
    private final boolean propagateExceptions;

    public AnnotatedMetricClassTransformer(Configuration config) {
        this(config, false);
    }

    public AnnotatedMetricClassTransformer(Configuration config, boolean propagateExceptions) {
        this.config = config;
        this.propagateExceptions = propagateExceptions;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        try {
            ClassReader cr = new ClassReader(classfileBuffer);

            // Scan for annotations in a pre-pass phase so we have all the metric information we need when performing
            // the actual instrumentation. This allows us to e.g. add Class Fields if desired for metrics which cannot
            // be done otherwise (as visitAnnotation happens after visitFieldInsn in ClassVisitor).
            scanMetricAnnotations(loader, cr);

            // rewrite only if metric found
            if (config.isMetric(className)) {
                ClassWriter cw = new ASMClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS, loader);
                ClassVisitor cv = new MetricClassVisitor(cw, config);
                cr.accept(cv, EXPAND_FRAMES);

                return cw.toByteArray();
            }

        } catch (RuntimeException e) {
            if (propagateExceptions) {
                throw e; //useful for testing & fail fast setups
            }
            else {
                LOGGER.log(WARNING, "Failed to transform " + className, e);
            }
        }

        return classfileBuffer;
    }

    private void scanMetricAnnotations(ClassLoader loader, ClassReader cr) {
        cr.accept(new AnnotationClassVisitor(new ASMClassWriter(0, loader), config), 0);
    }
}
