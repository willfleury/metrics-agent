package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.config.Configuration;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


/**
 *
 * @author Will Fleury
 */
public class AnnotationClassVisitor extends RestrictedClassVisitor {

    public AnnotationClassVisitor(ClassVisitor cv, Configuration config) {
        super(cv, config);
    }

    @Override
    public MethodVisitor visitAllowedMethod(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        return new AnnotationMethodVisitor(mv, config, className, name, desc);
    }
}
