package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.Metric;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ASM5;

/**
 *
 * @author Will Fleury
 */
public class MetricClassVisitor extends ClassVisitor {

    private boolean isInterface;
    private String className;
    private int classVersion;
    private Configuration config;

    public MetricClassVisitor(ClassVisitor cv, Configuration config) {
        super(ASM5, cv);
        this.config = config;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        this.classVersion = version;
        this.className = name;
        this.isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (!isInterface && mv != null) {
            List<Metric> metadata = config.findMetrics(className, name + desc);
            mv = new MetricAdapter(mv, access, name, desc, metadata);
            //TODO figure out.. Should only do this with old classes - same with frames only for 1.7+..
            // Only the JDK 1.5 and earlier compiler uses JSR and RET, so compiling for JDK 1.6 should be a workaround.
            //classversion is given above!
            mv = new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
        }

        return mv;
    }
}
