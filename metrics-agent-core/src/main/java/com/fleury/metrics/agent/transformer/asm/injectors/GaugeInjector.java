package com.fleury.metrics.agent.transformer.asm.injectors;

import com.fleury.metrics.agent.model.Metric;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury
 */
public class GaugeInjector extends AbstractInjector {

    private static final String INC_METHOD = "recordGaugeInc";
    private static final String DEC_METHOD = "recordGaugeDec";
    private static final String SIGNATURE = "(Ljava/lang/String;[Ljava/lang/String;)V";

    private final Metric metric;
    
    public GaugeInjector(Metric metric, AdviceAdapter aa, Type[] argTypes, int access) {
        super(aa, argTypes, access);
        this.metric = metric;
    }

    @Override
    public void injectAtMethodEnter() {
        injectNameAndLabelToStack(metric);

        String method = getMethod();
        aa.visitMethodInsn(INVOKESTATIC, METRIC_REPORTER_CLASSNAME, method, SIGNATURE, false);
    }

    private String getMethod() {
        return "inc".equals(metric.getExt().get("mode"))
                ? INC_METHOD : DEC_METHOD;
    }

}
