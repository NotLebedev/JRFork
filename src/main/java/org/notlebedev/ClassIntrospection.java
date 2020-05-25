package org.notlebedev;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ClassIntrospection extends ClassVisitor {
    private final Set<Class<?>> usedClasses;
    private final Set<Class<?>> classesInspected;

    public ClassIntrospection() {
        super(Opcodes.ASM7);
        usedClasses = new HashSet<>();
        classesInspected = new HashSet<>();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        try {
            System.out.println(forName(descriptor));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(signature);
        return super.visitField(access, name, descriptor, signature, value);
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
