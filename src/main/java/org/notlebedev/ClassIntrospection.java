package org.notlebedev;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ClassIntrospection {

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
