package org.notlebedev;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

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
            usedClasses.add(forName(descriptor));
        } catch (ClassNotFoundException e) {
            exceptions.add(e);
        }
        if(signature != null)
            try {
                usedClasses.addAll(forSignature(signature));
            } catch (ClassNotFoundException e) {
                exceptions.add(e);
            }
        return super.visitField(access, name, descriptor, signature, value);
    }

    private final static Pattern pattern1 = Pattern.compile("<(.*?)>");
    private final static Pattern pattern2 = Pattern.compile("(.*?;)");
    private static Set<Class<?>> forSignature(String signature) throws ClassNotFoundException {
        var result = new HashSet<Class<?>>();
        Matcher matcher = pattern1.matcher(signature);
        if(!matcher.find())
            throw new ClassNotFoundException();
        matcher = pattern2.matcher(matcher.group(1));
        if(!matcher.find())
            throw new ClassNotFoundException();
        do {
            result.add(forName(matcher.group(1)));
        }while (matcher.find());
        return result;
    }

    private static Class<?> forName(String name) throws ClassNotFoundException {
        name = name.replace(" ", "");

        switch (name) {

            case "boolean":
            case "Z":
                return boolean.class;
            case "byte":
            case "B":
                return byte.class;
            case "char":
            case "C":
                return char.class;
            case "long":
            case "J":
                return long.class;
            case "short":
            case "S":
                return short.class;
            case "int":
            case "I":
                return int.class;
            case "float":
            case "F":
                return float.class;
            case "double":
            case "D":
                return double.class;
            case "void":
                return void.class;
            default:
        }

        while (name.startsWith("["))
            name = name.substring(1);
        name = name.substring(1);
        name = name.replace(";", "");
        name = name.replace("/", ".");
        return Class.forName(name);
    }

    @Override
    public void visitEnd() {
        usedClasses.removeAll(classesKnown);
        classesKnown.addAll(usedClasses);
        Set<Class<?>> recursiveUsed = new HashSet<>();
        usedClasses.forEach(aClass -> {
            try {
                var introspection = new ClassIntrospection(aClass, classesKnown);
                recursiveUsed.addAll(introspection.getUsedClasses());
            } catch (SyntheticClassException | IOException ignored) {
            }
        });

        classesKnown.addAll(recursiveUsed);
        super.visitEnd();
    }
}
