package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import static org.objectweb.asm.Opcodes.ASM5;

/**
 * @author Will Fleury
 */
public class MetricAnnotationAttributeVisitor extends AnnotationVisitor {

    private final Configuration config;
    private final Configuration.Key metricKey;

    private Metric.MetricBuilder metricBuilder;

    public MetricAnnotationAttributeVisitor(AnnotationVisitor av, MetricType metricType, Configuration config, Configuration.Key metricKey) {
        super(ASM5, av);

        this.config = config;
        this.metricKey = metricKey;

        this.metricBuilder = Metric.builder().withType(metricType);
    }

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);

        if ("name".equals(name)) {
            metricBuilder.withName(value.toString());
        } else if ("doc".equals(name)) {
            metricBuilder.withDoc(value.toString());
        }
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        super.visit(name, value);

        if ("mode".equals(name)) {
            metricBuilder.withMode(value);
        }
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        if ("labels".equals(name)) {

            final List<String> labels = new ArrayList<String>();
            metricBuilder.withLabels(labels);

            return new AnnotationVisitor(ASM5, av) {

                @Override
                public void visit(String name, Object value) {
                    String label = value.toString();

                    if (!label.contains(":")) {
                        throw new IllegalArgumentException("Label: " + label + " is not format {name}:{value}");
                    }

                    labels.add(label);
                }
            };
        }

        return super.visitArray(name);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        Metric metric = metricBuilder.createMetric();

        List<Metric> metrics = config.getMetrics().get(metricKey);

        if (metrics == null) {
            metrics = new ArrayList<Metric>();
            config.getMetrics().put(metricKey, metrics);
        }

        metrics.add(metric);
    }
}
