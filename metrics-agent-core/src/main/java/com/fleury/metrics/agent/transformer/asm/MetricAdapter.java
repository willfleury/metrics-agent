package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.model.AnnotationScanner;
import com.fleury.metrics.agent.model.LabelUtil;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.transformer.asm.injectors.Injector;
import com.fleury.metrics.agent.transformer.asm.injectors.InjectorFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class MetricAdapter extends AdviceAdapter {

    private final Map<MetricType, Metric> metrics = new HashMap<MetricType, Metric>();
    private final AnnotationScanner annotationScanner;
    private final Type[] argTypes;
    private final String methodName;
    private final int access;
    
    private List<Injector> injectors;

    public MetricAdapter(List<Metric> configMetrics, MethodVisitor mv, int access, String name, String desc) {
        super(ASM5, mv, access, name, desc);

        this.methodName = name;
        this.argTypes = Type.getArgumentTypes(desc);
        this.access = access;
        this.annotationScanner = new AnnotationScanner(metrics);

        registerConfigurationMetrics(configMetrics);
    }

    private void registerConfigurationMetrics(List<Metric> configMetrics) {
        for (Metric metric : configMetrics) {
            metrics.put(metric.getType(), metric);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        Metric metatdata = annotationScanner.checkSignature(desc);
        if (metatdata != null) {
            return new MetricAnnotationAttributeVisitor(super.visitAnnotation(desc, visible), metatdata);
        }

        return super.visitAnnotation(desc, visible);
    }

    @Override
    protected void onMethodEnter() {
        injectors = InjectorFactory.createInjectors(metrics, this, argTypes, access);
        validateLabels();
        Reporter.registerMetrics(metrics.values());

        for (Injector injector : injectors) {
            injector.injectAtMethodEnter();
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        for (Injector injector : injectors) {
            injector.injectAtVisitMaxs(maxStack, maxLocals);
        }

        mv.visitMaxs(maxStack, maxLocals);
    }

    @Override
    protected void onMethodExit(int opcode) {
        for (Injector injector : injectors) {
            injector.injectAtMethodExit(opcode);
        }
    }

    private void validateLabels() {
        for (Metric metric : metrics.values()) {
            LabelUtil.validateLabelValues(methodName, metric.getLabels(), argTypes);
        }
    }
}
