package org.notlebedev.introspection;

import org.notlebedev.introspection.exceptions.SyntheticClassException;
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
    private final Set<Class<?>> omitClasses;
    private final List<ClassNotFoundException> exceptions;
    private final MethodIntrospection methodIntrospection;
    private final FieldIntrospection fieldIntrospection;
    private boolean inspectAnnotations;

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
        this.omitClasses = new HashSet<>(omitClasses);
        this.inspectAnnotations = true;
    }

    public Set<Class<?>> getUsedClasses() throws IOException, ClassNotFoundException {
        InputStream byteIn = clazz.getResourceAsStream(
                clazz.getTypeName().replace(clazz.getPackageName() + ".", "") + ".class");
        var cr = new ClassReader(byteIn);
        cr.accept(this, 0);
        if (!exceptions.isEmpty())
            throw exceptions.get(0);
        classesKnown.removeAll(omitClasses);
        return classesKnown;
    }

    /**
     * Should this introspection include annotation classes in result. True
     * (includes) by default
     * @param inspectAnnotations true -- include, false -- exclude
     */
    public void setInspectAnnotations(boolean inspectAnnotations) {
        this.inspectAnnotations = inspectAnnotations;
        methodIntrospection.setInspectAnnotations(inspectAnnotations);
        fieldIntrospection.setInspectAnnotations(inspectAnnotations);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        loadDescriptors(descriptor, signature);
        return fieldIntrospection;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        loadDescriptors(descriptor);
        return methodIntrospection;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        loadDescriptors(signature);
        loadNames(name, superName);
        for (String anInterface : interfaces) {
            loadNames(anInterface);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        loadDescriptors(descriptor);
        loadNames(name);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if(inspectAnnotations)
            loadDescriptors(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if(inspectAnnotations)
            loadDescriptors(descriptor);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        loadNames(name);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    /**
     * Load classes specified in descriptor in {@code usedClasses} {@link Set}
     *
     * @param descriptors descriptors with classes in format Lclass/path/name
     */
    private void loadDescriptors(String... descriptors) {
        for (String descriptor : descriptors) {
            try {
                usedClasses.addAll(ClassIntrospection.allClassNames(descriptor));
            } catch (ClassNotFoundException e) {
                exceptions.add(e);
            }
        }
    }

    private void loadNames(String... names) {
        for (String name : names) {
            try {
                Class<?> clazz = ClassIntrospection.forName(name);
                if(clazz != null)
                    usedClasses.add(clazz);
            } catch (ClassNotFoundException e) {
                exceptions.add(e);
            }
        }
    }

    private final static Pattern pattern = Pattern.compile("L(.*?)[;<]");

    static Set<Class<?>> allClassNames(String str) throws ClassNotFoundException {
        var result = new HashSet<Class<?>>();
        if (str == null)
            return result;
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            if (!JDKClassTester.isJDK(Class.forName(matcher.group(1).replace("/", "."))))
                result.add(Class.forName(matcher.group(1).replace("/", ".")));
        }

        return result;
    }

    private final static Pattern pattern2 = Pattern.compile("^([A-Za-z_0-9\\/]*)$");

    static Class<?> forName(String str) throws ClassNotFoundException {
        if(str == null)
            return null;
        Matcher matcher1 = pattern2.matcher(str);
        if (matcher1.find()) {
            char start = matcher1.group(1).charAt(0);
            if(matcher1.group(1).length() == 1 && (start == 'Z' || start == 'B' || start == 'C' || start == 'J' ||
                    start == 'S' || start == 'I' || start == 'F' || start == 'D')) //Check if this class is a primitive
                return null;
            if (!JDKClassTester.isJDK(Class.forName(matcher1.group(1).replace("/", "."))))
                return Class.forName(matcher1.group(1).replace("/", "."));
        }
        return null;
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
                introspection.setInspectAnnotations(inspectAnnotations);
                exceptions.addAll(introspection.getExceptions());
                classesKnown.addAll(introspection.getUsedClasses());
            } catch (SyntheticClassException | IOException | ClassNotFoundException ignored) {
            }
        });

        super.visitEnd();
    }

    private List<ClassNotFoundException> getExceptions() {
        return exceptions;
    }
}
