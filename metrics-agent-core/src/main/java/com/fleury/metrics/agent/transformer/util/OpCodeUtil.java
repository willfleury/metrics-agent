package com.fleury.metrics.agent.transformer.util;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;

/**
 *
 * @author Will Fleury
 */
public class OpCodeUtil {

    public static int getIConstOpcodeForInteger(int val) {
        switch (val) {
            case 0:
                return ICONST_0;
            case 1:
                return ICONST_1;
            case 2:
                return ICONST_2;
            case 3:
                return ICONST_3;
            case 4:
                return ICONST_4;
            case 5:
                return ICONST_5;

            default:
                throw new RuntimeException("No ICONST_ for int " + val);
        }
    }
}
