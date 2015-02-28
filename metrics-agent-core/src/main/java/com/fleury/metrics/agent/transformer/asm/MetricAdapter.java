package com.fleury.metrics.agent.transformer.asm;

import com.fleury.metrics.agent.model.AnnotationScanner;
import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.transformer.asm.injectors.Injector;
import com.fleury.metrics.agent.transformer.asm.injectors.InjectorFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 *
 * @author Will Fleury <will.fleury at boxever.com>
 */
public class MetricAdapter extends AdviceAdapter {
	
	private final LinkedHashMap<MetricType, Metric> metrics = new LinkedHashMap<MetricType, Metric>();
	private final List<Injector> injectors = new ArrayList<Injector>();
	private final AnnotationScanner annotationScanner;
	private final Type[] argTypes;

	public MetricAdapter(List<Metric> configMetrics, MethodVisitor mv, int access, String name, String desc) {
		super (ASM5, mv, access, name, desc);
		
		this.argTypes = Type.getArgumentTypes(desc);
		this.annotationScanner = new AnnotationScanner(metrics);
		
		registerConfigurationMetrics(configMetrics);
	}
	
	private void registerConfigurationMetrics(List<Metric> configMetrics) {
		for (Metric metric : configMetrics) {
			metrics.put(metric.getType(), metric);
			injectors.add(InjectorFactory.createInjector(metric, this, mv, argTypes));
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		Metric metatdata = annotationScanner.checkSignature(desc);
		if (metatdata != null) {
			injectors.add(InjectorFactory.createInjector(metatdata, this, mv, argTypes));
			
			return new MetricAnnotationAttributeVisitor(super.visitAnnotation(desc, visible), metatdata);
		}
		
		return super.visitAnnotation(desc, visible);
	}
	
	@Override
	protected void onMethodEnter() { 
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
}
