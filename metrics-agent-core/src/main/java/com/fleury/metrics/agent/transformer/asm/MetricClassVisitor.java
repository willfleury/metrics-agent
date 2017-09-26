package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.Metric;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 *
 * @author Will Fleury
 */
public class MetricClassVisitor extends RestrictedClassVisitor {

    public MetricClassVisitor(ClassVisitor cv, Configuration config) {
        super(cv, config);
    }

    public MethodVisitor visitAllowedMethod(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        List<Metric> metadata = config.findMetrics(className, name + desc);

        mv = new MetricAdapter(mv, className, access, name, desc, metadata);
        return new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
    }
}
