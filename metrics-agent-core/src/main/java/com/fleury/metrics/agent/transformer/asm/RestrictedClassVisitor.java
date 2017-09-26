package com.fleury.metrics.agent.transformer.asm;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ASM5;

import com.fleury.metrics.agent.config.Configuration;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public abstract class RestrictedClassVisitor extends ClassVisitor {

    protected boolean isInterface;
    protected String className;
    protected int classVersion;
    protected Configuration config;

    public RestrictedClassVisitor(ClassVisitor cv, Configuration config) {
        super(ASM5, cv);
        this.config = config;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.classVersion = version;
        this.className = name;
        this.isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        boolean isSyntheticMethod = (access & ACC_SYNTHETIC) != 0;

        if (!isInterface && !isSyntheticMethod && mv != null &&
                config.isWhiteListed(className) && !config.isBlackListed(className)) {
            mv = visitAllowedMethod(mv, access, name, desc, signature, exceptions);
        }

        return mv;
    }

    public abstract MethodVisitor visitAllowedMethod(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions);
}