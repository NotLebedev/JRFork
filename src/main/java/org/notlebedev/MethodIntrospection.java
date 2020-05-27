package org.notlebedev;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodIntrospection extends MethodVisitor {
    public MethodIntrospection() {
        super(Opcodes.ASM7);
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }
}
