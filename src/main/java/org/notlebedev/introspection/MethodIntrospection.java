package org.notlebedev.introspection;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MethodIntrospection extends MethodVisitor {
    private final Set<Class<?>> usedClasses;
    private final List<ClassNotFoundException> exceptions;
    private boolean inspectAnnotations;

    MethodIntrospection() {
        super(Opcodes.ASM7);
        usedClasses = new HashSet<>();
        exceptions = new ArrayList<>();
        this.inspectAnnotations = true;
    }

    /**
     * Should this introspection include annotation classes in result. True
     * (includes) by default
     * @param inspectAnnotations true -- include, false -- exclude
     */
    public void setInspectAnnotations(boolean inspectAnnotations) {
        this.inspectAnnotations = inspectAnnotations;
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        ldc(type);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        ldc(descriptor, owner);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        ldc(descriptor, bootstrapMethodHandle.getDesc());
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        ldc(type);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if(inspectAnnotations)
            ldc(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if(inspectAnnotations)
            ldc(descriptor);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        ldc(descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        if(inspectAnnotations)
            ldc(descriptor);
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        ldc(descriptor);
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if(inspectAnnotations)
            ldc(descriptor);
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if(inspectAnnotations)
            ldc(descriptor);
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        ldc(descriptor, signature);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        if(inspectAnnotations)
            ldc(descriptor);
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    /**
     * Load classes specified in descriptor in {@code usedClasses} {@link Set}
     *
     * @param descriptors descriptors with classes in format Lclass/path/name
     */
    private void ldc(String... descriptors) {
        for (String descriptor : descriptors) {
            try {
                usedClasses.addAll(ClassIntrospection.forDescriptor(descriptor));
            } catch (ClassNotFoundException e) {
                exceptions.add(e);
            }
        }
    }

    Set<Class<?>> getUsedClasses() {
        return usedClasses;
    }

    List<ClassNotFoundException> getExceptions() {
        return exceptions;
    }
}
