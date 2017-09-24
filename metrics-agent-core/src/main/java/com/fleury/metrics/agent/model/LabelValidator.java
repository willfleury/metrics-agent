package com.fleury.metrics.agent.model;


import org.objectweb.asm.Type;

/**
 *
 * @author Will Fleury
 */
public class LabelValidator {
    
    private final String method;
    private final Type[] argTypes;
    
    public LabelValidator(String method, Type[] argTypes) {
        this.method = method;
        this.argTypes = argTypes;
    }
    
    public void validate(String value) {
        if (value.startsWith("$this") && method.equals("<init>")) {
            throwLabelInvalidException(value, "Cannot use $this in Constructor");
        }

        if (value.startsWith("$")) {
            if (!value.matches("\\$([0-9]+|this)([a-zA-Z.]+)*")) {
                throwLabelInvalidException(value, "Must match pattern \\\\$([0-9]+|this)([a-zA-Z.]+)* or start with $this");
            }

            if (!value.startsWith("$this")) {

                int index = LabelUtil.getLabelVarIndex(value);

                if (index >= argTypes.length) {
                    throwLabelInvalidException(value, "It only has " + argTypes.length + " params");
                }

                Type argType = argTypes[index];
                if (argType.getSort() == Type.ARRAY) {
                    throwLabelInvalidException(value, "ARRAY type is not allowed");
                }
            }
        }
    }
    
    
    private void throwLabelInvalidException(String value, String reason) {
        throw new IllegalArgumentException(
                "Label value " + value + " for method " + method + " is invalid: " + reason);
    }

}
