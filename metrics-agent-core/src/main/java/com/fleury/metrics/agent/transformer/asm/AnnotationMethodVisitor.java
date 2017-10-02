package com.fleury.metrics.agent.transformer.asm;

import static com.fleury.metrics.agent.transformer.asm.util.AnnotationUtil.checkSignature;
import static org.objectweb.asm.Opcodes.ASM5;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.MetricType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;


/**
 *
 * @author Will Fleury
 */
public class AnnotationMethodVisitor extends MethodVisitor {

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
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        MetricType metricType = checkSignature(desc);

        if (metricType != null) {
            Configuration.Key key = new Configuration.Key(className, methodName, methodDesc);

            return new MetricAnnotationAttributeVisitor(super.visitAnnotation(desc, visible), metricType, config, key);
        }

        return super.visitAnnotation(desc, visible);
    }

}
