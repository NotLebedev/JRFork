package org.notlebedev.introspection;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassIntrospection extends ClassVisitor {
    private final Class<?> clazz;
    private final Set<Class<?>> usedClasses;
    private final Set<Class<?>> classesKnown;
    private final List<ClassNotFoundException> exceptions;
    private final MethodIntrospection methodIntrospection;
    private final FieldIntrospection fieldIntrospection;

    public ClassIntrospection(Class<?> clazz, Set<Class<?>> omitClasses) throws SyntheticClassException {
        super(Opcodes.ASM7);
        usedClasses = new HashSet<>();
        exceptions = new ArrayList<>();
        classesKnown = new HashSet<>(omitClasses);
        this.clazz = clazz;
        if (clazz.isSynthetic())
            throw new SyntheticClassException();
        methodIntrospection = new MethodIntrospection();
        fieldIntrospection = new FieldIntrospection();
    }

    public Set<Class<?>> getUsedClasses() throws IOException, ClassNotFoundException {
        InputStream byteIn = clazz.getResourceAsStream(
                clazz.getTypeName().replace(clazz.getPackageName() + ".", "") + ".class");
        var cr = new ClassReader(byteIn);
        cr.accept(this, 0);
        if (!exceptions.isEmpty())
            throw exceptions.get(0);
        return classesKnown;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        ldc(descriptor, signature);
        return fieldIntrospection;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        ldc(descriptor);
        return methodIntrospection;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        ldc(signature, superName);
        for (String anInterface : interfaces) {
            ldc(anInterface);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        ldc(owner, descriptor);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        ldc(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        ldc(descriptor);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        ldc(name);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    /**
     * Load classes specified in descriptor in {@code usedClasses} {@link Set}
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

    private final static Pattern pattern = Pattern.compile("L(.*?)[;<]");
    static Set<Class<?>> allClassNames(String str) throws ClassNotFoundException {
        var result = new HashSet<Class<?>>();
        if(str == null)
            return result;
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            result.add(Class.forName(matcher.group(1).replace("/", ".")));
        }
        return result;
    }

    @Override
    public void visitEnd() {
        exceptions.addAll(methodIntrospection.getExceptions());
        exceptions.addAll(fieldIntrospection.getExceptions());

        usedClasses.addAll(methodIntrospection.getUsedClasses());
        usedClasses.addAll(fieldIntrospection.getUsedClasses());
        usedClasses.removeAll(classesKnown);
        classesKnown.addAll(usedClasses);
        usedClasses.forEach(aClass -> {
            try {
                var introspection = new ClassIntrospection(aClass, classesKnown);
                exceptions.addAll(introspection.getExceptions());
                classesKnown.addAll(introspection.getUsedClasses());
            } catch (SyntheticClassException | IOException | ClassNotFoundException ignored) {
            }
        });

        super.visitEnd();
    }

    List<ClassNotFoundException> getExceptions() {
        return exceptions;
    }
}
