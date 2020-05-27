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
    private final List<Exception> exceptions;

    public ClassIntrospection(Class<?> clazz, Set<Class<?>> omitClasses) throws SyntheticClassException {
        super(Opcodes.ASM7);
        usedClasses = new HashSet<>();
        exceptions = new ArrayList<>();
        classesKnown = new HashSet<>(omitClasses);
        this.clazz = clazz;
        if (clazz.isSynthetic())
            throw new SyntheticClassException();
    }

    public Set<Class<?>> getUsedClasses() throws IOException {
        InputStream byteIn = clazz.getResourceAsStream(
                clazz.getTypeName().replace(clazz.getPackageName() + ".", "") + ".class");
        var cr = new ClassReader(byteIn);
        cr.accept(this, 0);
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
        return new MethodIntrospection();
    }

    private final static Pattern pattern = Pattern.compile("L(.*?)[;<]");
    private static Set<Class<?>> allClassNames(String str) throws ClassNotFoundException {
        var result = new HashSet<Class<?>>();
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            result.add(Class.forName(matcher.group(1).replace("/", ".")));
        }
        return result;
    }

    @Override
    public void visitEnd() {
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

        classesKnown.addAll(recursiveUsed);
        super.visitEnd();
    }



    public List<Exception> getExceptions() {
        return exceptions;
    }
}
