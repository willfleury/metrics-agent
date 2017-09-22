package com.fleury.metrics.agent.transformer.asm;

import static java.util.logging.Level.FINE;

import com.fleury.metrics.agent.model.AnnotationScanner;
import com.fleury.metrics.agent.model.LabelUtil;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.transformer.asm.injectors.Injector;
import com.fleury.metrics.agent.transformer.asm.injectors.InjectorFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class MetricAdapter extends AdviceAdapter {

    private static final Logger LOGGER = Logger.getLogger(AdviceAdapter.class.getName());

    //all classes that are modified will be registered in this cache so we can determine
    //if later in processing if it was touched & transformed
    public static final Set<String> MODIFIED_CLASS_CACHE = new CopyOnWriteArraySet<String>();

    private final Map<MetricType, Metric> metrics = new HashMap<MetricType, Metric>();
    private final AnnotationScanner annotationScanner;
    private final Type[] argTypes;
    private final String className;
    private final String methodName;
    private final int access;
    
    private List<Injector> injectors;

    public MetricAdapter(MethodVisitor mv, String className, int access, String name, String desc, List<Metric> metadata) {
        super(ASM5, mv, access, name, desc);

        this.className = className;
        this.methodName = name;
        this.argTypes = Type.getArgumentTypes(desc);
        this.access = access;
        this.annotationScanner = new AnnotationScanner(metrics);

        registerConfigurationMetrics(metadata);
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
        if (metrics.isEmpty()) {
            injectors = Collections.emptyList();
            return;
        }

        MODIFIED_CLASS_CACHE.add(className);
        LOGGER.log(FINE, "Metrics found on : {0}", methodName);

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
