package com.fleury.metrics.agent.transformer.asm.injectors;

import static com.fleury.metrics.agent.config.Configuration.dotToSlash;

import com.fleury.metrics.agent.config.Configuration;
import com.fleury.metrics.agent.reporter.Reporter;
import com.fleury.metrics.agent.reporter.TestMetricSystem;
import com.fleury.metrics.agent.transformer.AnnotatedMetricClassTransformer;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 *
 * @author Will Fleury
 */
public abstract class BaseMetricTest {

    private static final Logger LOGGER = Logger.getLogger(BaseMetricTest.class.getName());

    protected TestMetricSystem metrics;

    @Before
    public void setup() {
        metrics = (TestMetricSystem) Reporter.METRIC_SYSTEM;
        metrics.reset();
    }

    private final ByteCodeClassLoader loader = new ByteCodeClassLoader();

    @SuppressWarnings("unchecked")
    public <T> Class<T> getClassFromBytes(Class<T> clazz, byte[] bytes) {
        return loader.defineClass(clazz.getName(), bytes);
    }

    protected <T> Class<T> execute(Class<T> clazz) throws Exception {
        return execute(clazz, new Configuration());
    }

    protected <T> Class<T> execute(Class<T> clazz, Configuration config) throws Exception {
        String className = dotToSlash(clazz.getName());
        String classAsPath = className + ".class";

        ClassFileTransformer cft = new AnnotatedMetricClassTransformer(config, true);
        byte[] classfileBuffer = cft.transform(
                clazz.getClassLoader(),
                className,
                null,
                null,
                IOUtils.toByteArray(clazz.getClassLoader().getResourceAsStream(classAsPath)));

        traceBytecode(classfileBuffer);
        verifyBytecode(classfileBuffer);

        return getClassFromBytes(clazz, classfileBuffer);
    }
    
    private void traceBytecode(byte[] bytecode) {
        ClassReader cr = new ClassReader(bytecode);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(new TraceClassVisitor(cw, new PrintWriter(System.out)), 0);
    }

    private void verifyBytecode(byte[] bytecode) {
        ClassReader cr = new ClassReader(bytecode);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(new CheckClassAdapter(cw), 0);
    }

    public static class ByteCodeClassLoader extends ClassLoader {

        public Class defineClass(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    public static void performBasicTask() {
        LOGGER.fine("Debugging to ensure basic op perfomred by calling code");
    }
}
