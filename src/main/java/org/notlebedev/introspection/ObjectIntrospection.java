package org.notlebedev.introspection;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ObjectIntrospection {
    private final Object containedClass;
    private final Set<Class<?>> classesUsed;
    private final Set<Class<?>> omitClasses;

    public ObjectIntrospection(Object obj) {
        containedClass = obj;
        classesUsed = new HashSet<>();
        omitClasses = new HashSet<>();
    }

    public ObjectIntrospection(Object obj, Set<Class<?>> omitClasses) {
        containedClass = obj;
        classesUsed = new HashSet<>();
        this.omitClasses = new HashSet<>(omitClasses);
    }

    /**
     * This method will inspect all data in contained class and determine all classes used in fields with inheritance
     * taking place (e.g. if an Integer is stored in Object field Integer class will be determined) and do so
     * recursively
     */
    public void inspectData() {
        inspectDataRecursion(containedClass, classesUsed);
        classesUsed.removeAll(omitClasses);
    }

    private void inspectDataRecursion(Object obj, Set<Class<?>> classesUsed) {
        if(obj == null)
            return;
        Class<?> baseClass = obj.getClass();
        if(omitClasses.contains(baseClass) || JDKClassTester.isJDK(baseClass))
            return;
        classesUsed.add(baseClass);
        for (Field baseClassField : baseClass.getDeclaredFields()) {
            baseClassField.setAccessible(true);
            try {
                inspectDataRecursion(baseClassField.get(obj), classesUsed);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (StackOverflowError e) {
                System.out.println(baseClass);
                throw new StackOverflowError();
            }
            baseClassField.setAccessible(false);
        }
    }
}
