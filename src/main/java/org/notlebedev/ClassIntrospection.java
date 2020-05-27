package org.notlebedev;

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

    public ClassIntrospection(Class<?> clazz, Set<Class<?>> omitClasses) throws SyntheticClassException {
        super(Opcodes.ASM7);
        usedClasses = new HashSet<>();
        exceptions = new ArrayList<>();
        classesKnown = new HashSet<>(omitClasses);
        this.clazz = clazz;
        if (clazz.isSynthetic())
            throw new SyntheticClassException();
        methodIntrospection = new MethodIntrospection();
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
        try {
            usedClasses.addAll(allClassNames(descriptor));
        } catch (ClassNotFoundException e) {
            exceptions.add(e);
        }
        if(signature != null)
            try {
                usedClasses.addAll(allClassNames(signature));
            } catch (ClassNotFoundException e) {
                exceptions.add(e);
            }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        try {
            usedClasses.addAll(allClassNames(descriptor));
        } catch (ClassNotFoundException e) {
            this.exceptions.add(e);
        }
        return methodIntrospection;
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

        usedClasses.addAll(methodIntrospection.getUsedClasses());
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
