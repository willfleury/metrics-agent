package com.fleury.metrics.agent.introspector;

import static java.util.logging.Level.FINE;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanIntrospector;
import org.apache.commons.beanutils.IntrospectionContext;

public class GenericClassIntrospector implements BeanIntrospector {

    private static final Logger LOGGER = Logger.getLogger(GenericClassIntrospector.class.getName());

    @Override
    public void introspect(IntrospectionContext icontext) {

        for (final Method m : icontext.getTargetClass().getMethods()) {

            if (isValidValueMethod(m)) {
                try {
                    icontext.addPropertyDescriptor(new PropertyDescriptor(m.getName(), m, null));
                } catch (final IntrospectionException e) {
                    LOGGER.info("Error when creating PropertyDescriptor for " + m + "! Ignoring this property.");
                    LOGGER.log(FINE, "Exception is:", e);
                }
            }
        }
    }

    private boolean isValidValueMethod(Method m) {
        //e.g. name()
        return m.getParameterTypes().length == 0 &&
                !m.getReturnType().equals(Void.TYPE) &&
                !m.getName().startsWith("get") && //already obtained via DefaultBeanIntrospector
                !m.getName().startsWith("wait") &&
                !m.getName().equals("hashCode") &&
                !m.getName().equals("toString");
    }
}
