package com.fleury.metrics.agent.transformer.asm;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ASM5;

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

        boolean isSyntheticMethod = (access & ACC_SYNTHETIC) != 0;
//        boolean isBridge = (access & ACC_BRIDGE) != 0;

        if (!isInterface && !isSyntheticMethod && mv != null &&
                config.isWhiteListed(className) && !config.isBlackListed(className)) {
            List<Metric> metadata = config.findMetrics(className, name + desc);

            mv = new MetricAdapter(mv, className, access, name, desc, metadata);
            mv = new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
        }

        return mv;
    }
}
