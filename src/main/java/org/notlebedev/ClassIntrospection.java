package org.notlebedev;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassIntrospection extends ClassVisitor {
    private final Class<?> clazz;
    private final Set<Class<?>> usedClasses;
    private final Set<Class<?>> classesInspected;
    private final List<Exception> exceptions;

    public ClassIntrospection(Class<?> clazz, Set<Class<?>> omitClasses) throws SyntheticClassException {
        super(Opcodes.ASM7);
        usedClasses = new HashSet<>();
        exceptions = new ArrayList<>();
        classesInspected = new HashSet<>(omitClasses);
        this.clazz = clazz;
        if (clazz.isSynthetic())
            throw new SyntheticClassException();
    }

    public Set<Class<?>> getUsedClasses() throws IOException {
        InputStream byteIn = clazz.getResourceAsStream(
                clazz.getTypeName().replace(clazz.getPackageName() + ".", "") + ".class");
        var cr = new ClassReader(byteIn);
        cr.accept(this, 0);
        return usedClasses;
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
        super.visitEnd();
    }

    public static Set<Class<?>> getUsedClasses(Class<?> clazz, Set<Class<?>> classesKnown) {
        Set<Class<?>> usedClasses = new HashSet<>();
        usedClasses.addAll(Arrays.stream(clazz.getDeclaredFields()).map(Field::getType)
                .collect(Collectors.toCollection(ArrayList::new)));
        usedClasses.addAll(Arrays.stream(clazz.getDeclaredClasses())
                .collect(Collectors.toCollection(ArrayList::new)));

        usedClasses.addAll(Arrays.stream(clazz.getDeclaredMethods()).map(Method::getReturnType)
                .collect(Collectors.toCollection(ArrayList::new)));
        usedClasses.addAll(Arrays.stream(clazz.getDeclaredMethods()).map(Method::getParameterTypes).flatMap(Arrays::stream)
                .collect(Collectors.toCollection(ArrayList::new)));
        usedClasses.addAll(Arrays.stream(clazz.getDeclaredMethods()).map(Method::getExceptionTypes).flatMap(Arrays::stream)
                .collect(Collectors.toCollection(ArrayList::new)));

        usedClasses.addAll(Arrays.stream(clazz.getConstructors()).map(Constructor::getParameterTypes)
                .flatMap(Arrays::stream)
                .collect(Collectors.toCollection(ArrayList::new)));
        usedClasses.addAll(Arrays.stream(clazz.getConstructors()).map(Constructor::getExceptionTypes)
                .flatMap(Arrays::stream)
                .collect(Collectors.toCollection(ArrayList::new)));

        if(clazz.getComponentType() != null)
            usedClasses.add(clazz.getComponentType()); //If class is array add its component type

        usedClasses.removeAll(classesKnown);
        classesKnown.addAll(usedClasses);

        Set<Class<?>> recursiveUsed = new HashSet<>();
        usedClasses.forEach(aClass -> recursiveUsed.addAll(getUsedClasses (aClass, classesKnown)));

        classesKnown.addAll(recursiveUsed);

        classesKnown.removeIf(Class::isArray);
        return classesKnown;
    }

}
