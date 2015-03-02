package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.model.Metric;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import static org.objectweb.asm.Opcodes.ASM5;

/**
 * @author Will Fleury
 */
public class MetricAnnotationAttributeVisitor extends AnnotationVisitor {

    private final Metric metric;

    public MetricAnnotationAttributeVisitor(AnnotationVisitor av, Metric metric) {
        super(ASM5, av);

        this.metric = metric;
    }

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);

        if ("name".equals(name)) {
            metric.setName(value.toString());
        } else if ("doc".equals(name)) {
            metric.setDoc(value.toString());
        } else {
            metric.getExt().put(name, value.toString());
        }
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        super.visit(name, value);

        metric.getExt().put(name, value);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        if ("labels".equals(name)) {
            return new AnnotationVisitor(ASM5, av) {

                @Override
                public void visit(String name, Object value) {
                    String label = value.toString();

                    if (!label.contains(":")) {
                        throw new IllegalArgumentException("Label: " + label + " is not format {name}:{value}");
                    }

                    List<String> labels = metric.getLabels();
                    if (labels == null) {
                        labels = new ArrayList<String>();
                        metric.setLabels(labels);
                    }

                    labels.add(label);
                }
            };
        }

        return super.visitArray(name);
    }
}
