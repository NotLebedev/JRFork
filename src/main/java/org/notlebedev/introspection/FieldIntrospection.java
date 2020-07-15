package org.notlebedev.introspection;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FieldIntrospection extends FieldVisitor {
    private final Set<Class<?>> usedClasses;
    private final List<ClassNotFoundException> exceptions;
    private boolean inspectAnnotations;

    FieldIntrospection() {
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

    /**
     * Load classes specified in descriptor in {@code usedClasses} {@link Set}
     *
     * @param descriptors descriptors with classes in format Lclass/path/name
     */
    private void ldc(String... descriptors) {
        for (String descriptor : descriptors) {
            try {
                usedClasses.addAll(ClassIntrospection.allClassNames(descriptor));
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
