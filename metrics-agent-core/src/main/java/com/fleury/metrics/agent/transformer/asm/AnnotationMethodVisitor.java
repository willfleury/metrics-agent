package com.fleury.metrics.agent.transformer.asm;

import static org.objectweb.asm.Opcodes.ASM5;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.AnnotationScanner;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;


/**
 *
 * @author Will Fleury
 */
public class AnnotationMethodVisitor extends MethodVisitor {

    private final Map<MetricType, Metric> metrics = new HashMap<MetricType, Metric>();
    private final AnnotationScanner annotationScanner;
    private final Configuration config;
    private final String className;
    private final String methodName;
    private final String methodDesc;

    public AnnotationMethodVisitor(MethodVisitor mv, Configuration config, String className, String name, String desc) {
        super(ASM5, mv);

        this.config = config;
        this.className = className;
        this.methodName = name;
        this.methodDesc = desc;

        this.annotationScanner = new AnnotationScanner(metrics);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        Metric metric = annotationScanner.checkSignature(desc);

        if (metric != null) {
            Configuration.Key key = new Configuration.Key(className, methodName + methodDesc);
            List<Metric> metrics = config.getMetrics().get(key);

            if (metrics == null) {
                metrics = new ArrayList<Metric>();
                config.getMetrics().put(key, metrics);
            }

            metrics.add(metric);

            return new MetricAnnotationAttributeVisitor(super.visitAnnotation(desc, visible), metric);
        }

        return super.visitAnnotation(desc, visible);
    }

}
